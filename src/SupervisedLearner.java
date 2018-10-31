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
	static Random rand = new Random();
	String name()
	{
		return "DecisionTree";
	}
	// int[] pickDividingColumnAndPivot(Matrix feat)
	// {
	// 	int col = rand.next(feat.cols());
	// 	int row = rand.next(feat.rows());
	// 	double pivot = feat.row(row)[col];
	// 	return new int[] {col, pivot};
	// }
	int pickDividingColumn(Matrix feat)
	{
		int col = rand.nextInt(feat.cols());
		return col;
	}
	double pickPivot(Matrix feat)
	{
		int col = rand.nextInt(feat.cols());
		int row = rand.nextInt(feat.rows());
		double pivot = feat.row(row)[col];
		return pivot;
	}
	Node buildTree(Matrix feat, Matrix labels)
	{
		if(feat.rows() != labels.rows())
			throw new IllegalArgumentException("Mismatching features and labels");
		int col = pickDividingColumn(feat);// Continuous or Categorical
		double pivot = pickPivot(feat);

		// Divide the data.
		Matrix featLeft = new Matrix(feat);
		Matrix featRight = new Matrix(feat);
		Matrix labLeft = new Matrix(labels);
		Matrix labRight = new Matrix(labels);
		for(int j = 8; j > 0; j--) // This number 8 is subject to change.
		{
			int vals = feat.valueCount(col);
			// Loop to divide data.
			for(int j = 0; j < feat.rows(); j++)
			{	// Continuous
				if(vals == 0)
				{
					if(feat.row(i)[col] < pivot) // TODO: Find out how to access this value.
					{
						featLeft.takeRow(feat.removeRow(i));
						labLeft.takeRow(labels.removeRow(i));
					}
					else
					{
						featRight.takeRow(feat.removeRow(i));
						labRight.takeRow(labels.removeRow(i));
					}
				}
				else // Categorical
				{
					// Divide on categorical values
					if(feat.row(i)[col] == pivot)
					{
						featLeft.takeRow(feat.removeRow(i));
						labLeft.takeRow(labels.removeRow(i));
					}
					else
					{
						featRight.takeRow(feat.removeRow(i));
						labRight.takeRow(labels.removeRow(i));
					}
				}
			}
			// LeafNode case
			if(featLeft.rows() == 0 || featRight.rows() == 0)
				 break;//return new LeafNode(labels); // Similar to BaseLineLearner training.
		}
		if(featLeft.rows() == 0 || featRight.rows() == 0)
			return new LeafNode(labels);
		// Make the node
		Node left = buildTree(featLeft, labLeft);
		Node right = buildTree(featRight, labRight);
		return new InteriorNode(left, right, col, pivot);
	}
	void train(Matrix features, Matrix labels)
	{
		// Build a decision tree recursively.
		this.root = buildTree(features, labels);
	}
	void predict(double[] in, double[] out)
	{
		Node n = root;
		while(true)
		{
			if(!n.isLeaf()) // This means the Node is an InterioNode
			{
				if(in[n.getAttribute()] < n.getPivot())
					n = n.getA();
				else
					n = n.getB();
			}
			else
			{
				Vec.copy(out, n.getLabels());
				// return n.labels;
			}
		}
	}
}
class RandomForest extends SupervisedLearner
{
	String name()
	{
		return "RandomForest";
	}
	void train(Matrix features, Matrix labels)
	{

	}
	void predict(double[] in, double[] out)
	{

	}
}
