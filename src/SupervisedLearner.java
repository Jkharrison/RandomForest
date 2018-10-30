// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

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
	int[] pickDividingColumnAndPivot(Matrix feat)
	{
		int col = rand.next(feat.cols());
		int row = rand.next(feat.rows());
		double pivot = feat[row][col];
		return new int[] {col, pivot};
	}
	int pickDividingColumn(Matrix feat)
	{
		int col = rand.next(feat.cols());
		return col;
	}
	double pickPivot(Matrix feat)
	{
		int col = rand.next(feat.cols());
		int row = rand.next(feat.rows());
		double pivot = feat[row][col];
		return pivot;
	}
	Node buildTree(Matrix feat, Matrix labels)
	{
		if(feat.rows() != labels.rows())
			throw new IllegalArgumentException("Mismatching features and labels");
		int col = pickDividingColumn(feat);// Continuous or Categorical
		double pivot = pickPivot(feat);

		// Divide the data.
		for(int i = 8; i > 0; i--)
		{
			int vals = feat.valueCount(col);
			Matrix featLeft(feat);
			Matrix featRight(feat);
			Matrix labLeft(feat);
			Matrix labRight(feat);
			// Loop to divide data.
			for(int i = 0; i < feat.rows(); i++)
			{	// Continuous
				if(vals == 0)
				{
					if(feat[i][col]) < pivot)
					{
						featLeft.takeRow(feat.removeRow(i));
						labLeft.takeRow(feat.removeRow(i));
					}
					else
					{
						featRight.takeRow(feat.removeRow(i));
						labRight.takeRow(feat.removeRow(i));
					}
				}
				else // Categorical
				{
					// Divide on categorical values
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
			if(!n.isLeaf())
			{
				if(feat[n.col] < n.pivot)
					n = n.a;
				else
					n = n.b;
			}
			else
			{
				return n.labels;
			}
		}
	}
}
