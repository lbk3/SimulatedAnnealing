import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class StartOptimiser {
	public static int nSc;

	public static void main(String argv[]) throws Exception {
		// nSc = Integer.parseInt(argv[0]);
		System.out.println("*****************************");
		KusiakLayoutEvaluator eval = new KusiakLayoutEvaluator();
		WindScenario sc = new WindScenario("./Scenarios/practice_" + "3" + ".xml");
		eval.initialize(sc);
		System.out.println("Loading File " + sc);
		Solver algorithm = new Solver(eval);
		algorithm.run_cw();
		System.out.println("*****************************");
		System.out.println("Completed");
	}
}