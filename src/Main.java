// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
import java.util.ArrayList;
// import org.apache.commons.lang3.ArrayUtils;
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
		testLearner(new DecisionTree());
		testLearner(new RandomForest(30));
	}
}
abstract class Node
{
	abstract boolean isLeaf();
	abstract Node getA();
	abstract Node getB();
	abstract int getAttribute();
	abstract double getPivot();
	abstract double[] getLabels();
}
class InteriorNode extends Node
{
	int attribute; // Which attribute to divide on
	double pivot;	// which value to divide on
	Node a;
	Node b;
	InteriorNode(Node one, Node two, int i , double p)
	{
		// System.out.println("Creating new Interior Node");
		this.a = one;
		this.b = two;
		this.attribute = i;
		this.pivot = p;
	}
	boolean isLeaf()
	{
		return false;
	}
	Node getA()
	{
		return this.a;
	}
	Node getB()
	{
		return this.b;
	}
	int getAttribute()
	{
		return this.attribute;
	}
	double getPivot()
	{
		return this.pivot;
	}
	double[] getLabels()
	{
		return null;
	}
}
class LeafNode extends Node
{
	double[] labels;
	LeafNode()
	{
		this.labels = null;
	}
	LeafNode(Matrix Labels)
	{
		// System.out.println("Creating new leaf node from Matrix parameter");
		// TODO: Combine K label vectors into one label vector
		// TODO: Implementation below is also incorrect.
		double[] temp = new double[Labels.cols()];
		// System.out.println("Temp's Length: " + temp.length);
		for(int i = 0; i < Labels.cols(); i++)
		{
			if(Labels.valueCount(i) == 0)
				temp[i] = Labels.columnMean(i);
			else
				temp[i] = Labels.mostCommonValue(i);
		}
		this.labels = temp;
//		for(int i = 0; i < Labels.rows(); i++)
//		{
//			// System.out.println("Length of temp is: " + temp.length);
//			temp = Vec.concatenate(temp, Labels.row(i)); // NOTE: Might be causing the terribleness of my Decision Tree. xD
//		}
		// this.labels = temp;
		// System.out.println("Labels.length = " + this.labels.length);
	}
	LeafNode(double[] labs)
	{
		this.labels = labs;
	}
	boolean isLeaf()
	{
		return true;
	}
	Node getA()
	{
		return null;
	}
	Node getB()
	{
		return null;
	}
	int getAttribute()
	{
		return -1;
	}
	double getPivot()
	{
		return -1;
	}
	double[] getLabels()
	{
		return this.labels;
	}
 }
