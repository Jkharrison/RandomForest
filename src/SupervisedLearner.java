// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
import java.util.Random;
abstract class SupervisedLearner
{
	/// Return the name of this learner
	abstract String name();

	/// Train this supervised learner
	abstract void train(Matrix features, Matrix labels);

	/// Make a prediction
	abstract void predict(double[] in, double[] out);

	/// Measures the misclassifications with the provided test data
	int countMisclassifications(Matrix features, Matrix labels)
	{
		if(features.rows() != labels.rows())
			throw new IllegalArgumentException("Mismatching number of rows");
		double[] pred = new double[labels.cols()];
		int mis = 0;
		for(int i = 0; i < features.rows(); i++)
		{
			double[] feat = features.row(i);
			predict(feat, pred);
			double[] lab = labels.row(i);
			for(int j = 0; j < lab.length; j++)
			{
				if(pred[j] != lab[j])
					mis++;
			}
		}
		return mis;
	}
}
class DecisionTree extends SupervisedLearner
{
	Node root;
	Matrix feat;
	static Random rand = new Random((long)30.0);
	String name()
	{
		return "DecisionTree";
	}
	int pickDividingColumn(Matrix feat)
	{
		// rand.setSeed(25);
		int col = rand.nextInt(feat.cols());
		return col;
	}
	double pickPivot(Matrix feat, int col)
	{
		// rand.setSeed(75);
		// System.out.println(feat.rows());
		int row;
		if(feat.rows() > 0)
			row = rand.nextInt(feat.rows());
		else
			row = 0;
		double pivot = feat.row(row)[col];
		return pivot;
	}
	Node buildTree(Matrix feat, Matrix labels)
	{
		if(feat.rows() != labels.rows())
			throw new IllegalArgumentException("Mismatching features and labels");
		int col = 0; // Continuous or Categorical
		double pivot = 0.0;
		// this.features = new Matrix(feat);
		// Divide the data.
		Matrix featLeft = new Matrix();
		featLeft.copyMetaData(feat);
		Matrix featRight = new Matrix();
		featRight.copyMetaData(feat);
		Matrix labLeft = new Matrix();
		labLeft.copyMetaData(labels);
		Matrix labRight = new Matrix();
		labRight.copyMetaData(labels);
		Matrix featCopy = new Matrix(feat);
		Matrix labCopy = new Matrix(labels);
		for(int j = 8; j > 0; j--) // This number 8 is subject to change.
		{
			col = pickDividingColumn(feat);
			pivot = pickPivot(feat, col);
			int vals = feat.valueCount(col);
			// Loop to divide data.
			featLeft = new Matrix();
			featLeft.copyMetaData(feat);

			featRight = new Matrix();
			featRight.copyMetaData(feat);

			labLeft = new Matrix();
			labLeft.copyMetaData(labels);

			labRight = new Matrix();
			labRight.copyMetaData(labels);
			
			featCopy.copy(feat);
			labCopy.copy(labels);
			int i = 0;
			while(featCopy.rows() != 0)
			{	// Continuous
				if(vals == 0)
				{
					if(featCopy.row(i)[col] <= pivot)
					{
						featLeft.takeRow(featCopy.removeRow(i));
						labLeft.takeRow(labCopy.removeRow(i));
					}
					else
					{
						featRight.takeRow(featCopy.removeRow(i));
						labRight.takeRow(labCopy.removeRow(i));
					}
				}
				else // Categorical
				{
					// Divide on categorical values
					if(featCopy.row(i)[col] == pivot)
					{
						featLeft.takeRow(featCopy.removeRow(i));
						labLeft.takeRow(labCopy.removeRow(i));
					}
					else
					{
						featRight.takeRow(featCopy.removeRow(i));
						labRight.takeRow(labCopy.removeRow(i));
					}
				}
			}
			// LeafNode case
			if(featLeft.rows() != 0 && featRight.rows() != 0)
			{
				// System.out.println("Found good breaking point");
				break;//return new LeafNode(labels); // Similar to BaseLineLearner training.
			}
		}
		if(featLeft.rows() == 0 || featRight.rows() == 0)
		{
			// System.out.println("Bad split but creating leaf node");
			return new LeafNode(labels);
		}
		// Make the node
		// System.out.println("Not ending train method");
		Node left = buildTree(featLeft, labLeft);
		Node right = buildTree(featRight, labRight);
		return new InteriorNode(left, right, col, pivot);
	}
	void train(Matrix features, Matrix labels)
	{
		// Build a decision tree recursively.
		this.feat = new Matrix(features);
		this.root = buildTree(features, labels);
	}
	void predict(double[] in, double[] out)
	{
		Node n = root;
		while(true)
		{
			// System.out.println("Not breaking in this loop");
			if(!n.isLeaf()) // This means the Node is an InteriorNode
			{
				int vals = this.feat.valueCount(n.getAttribute());
				// TODO: Differentiate between continous/categorical
				if(vals == 0) // Continous
				{
					if(in[n.getAttribute()] <= n.getPivot())
						n = n.getA();
					else
						n = n.getB();
				}
				else // Categorical
				{
					if(in[n.getAttribute()] == n.getPivot())
						n = n.getA();
					else
						n = n.getB();
				}
			}
			else
			{
				Vec.copy(out, n.getLabels());
				break;
				// return n.labels;
			}
		}
	}
}
class RandomForest extends SupervisedLearner
{
	DecisionTree[] trees;
	static Random rand = new Random((long) 25.0);
	Matrix featRef;
	RandomForest(int n)
	{
		this.trees = new DecisionTree[n];
	}
	String name()
	{
		return "RandomForest";
	}
	void train(Matrix features, Matrix labels)
	{
		featRef = new Matrix(features);
		for(int i = 0; i < trees.length; i++)
		{
			trees[i] = new DecisionTree();
		}
		// Generate new training data for each decisiontree by sampling with replacement.
		// Then train all the trees.
		for(int i = 0; i < trees.length; i++)
		{
			// Take a copy of the features matrix.
			Matrix currentCopy = new Matrix();
			currentCopy.copyMetaData(features);
			Matrix currentLabelsCopy = new Matrix();
			currentLabelsCopy.copyMetaData(labels);
			for(int j = 0; j < features.rows(); j++)
			{
				int val = rand.nextInt(features.rows());
				currentCopy.takeRow(features.row(val)); // Grab random rows from features matrix.
				currentLabelsCopy.takeRow(labels.row(val));
			}
			trees[i].train(currentCopy, currentLabelsCopy);
		}
	}
	void predict(double[] in, double[] out)
	{
		Matrix votes = new Matrix();
		double[] tempOut = new double[out.length];
		for(int i = 0; i < trees.length; i++)
		{
			trees[i].predict(in, tempOut); // Error, mismatching sizes.
			votes.takeRow(tempOut);
		}
		out = new double[votes.cols()];
		for(int i = 0; i < votes.cols(); i++)
		{
			out[i] = votes.mostCommonValue(i);
		}
		// Use some data structure to store the votes of each tree.
		// Then have some output based on each of the votes.
	}
}
