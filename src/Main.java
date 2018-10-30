// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

class Main
{
	static void test(SupervisedLearner learner, String challenge)
	{
		// Load the training data
		String fn = "data/" + challenge;
		Matrix trainFeatures = new Matrix();
		trainFeatures.loadARFF(fn + "_train_feat.arff");
		Matrix trainLabels = new Matrix();
		trainLabels.loadARFF(fn + "_train_lab.arff");

		// Train the model
		learner.train(trainFeatures, trainLabels);

		// Load the test data
		Matrix testFeatures = new Matrix();
		testFeatures.loadARFF(fn + "_test_feat.arff");
		Matrix testLabels = new Matrix();
		testLabels.loadARFF(fn + "_test_lab.arff");

		// Measure and report accuracy
		int misclassifications = learner.countMisclassifications(testFeatures, testLabels);
		System.out.println("Misclassifications by " + learner.name() + " at " + challenge + " = " + Integer.toString(misclassifications) + "/" + Integer.toString(testFeatures.rows()));
	}

	public static void testLearner(SupervisedLearner learner)
	{
		test(learner, "hep");
		test(learner, "vow");
		test(learner, "soy");
	}

	public static void main(String[] args)
	{
		testLearner(new BaselineLearner());
		//testLearner(new RandomForest(50));
	}
}
abstract class Node
{
	abstract boolean isLeaf();
}
class InteriorNode extends Node
{
	int attribute; // Which attribute to divide on
	double pivot;	// which value to divide on
	Node a;
	Node b;
	InteriorNode(Node one, Node two, int i , double p)
	{
		this.a = one;
		this.b = two;
		this.attribute = i;
		this.pivot = p;
	}
	boolean isLeaf()
	{
		return false;
	}
}
class LeafNode extends Node
{
	double[] labels;
	LeafNode(double[] labs)
	{
		this.labels = labs;
	}
	boolean isLeaf()
	{
		return true;
	}
}
