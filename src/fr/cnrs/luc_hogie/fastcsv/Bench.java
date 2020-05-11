package fr.cnrs.luc_hogie.fastcsv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import jexperiment.Configuration;
import jexperiment.Experiment;
import jexperiment.Function;
import jexperiment.Plot;
import toools.io.fast_input_stream.PagingInputStream;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.progression.LongProcess;

public class Bench {
	static int nbLinesInFile = 1000000;
	static RegularFile csvFile = new RegularFile("$HOME/" + nbLinesInFile + ".csv");

	interface Test {
		String getName();

		void f(int nbLines, int nbColums) throws IOException;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("creating CSV text");
		int nbColumns = 10;
		byte[] line = createLine(nbColumns).getBytes();
		Experiment experiment = new Experiment(new Directory("bench"));
		Plot plot = experiment.createPlot(
				"Speed when parsing CSV file of numerical values", "#lines", "time (ns)");

		List<Test> tests = new ArrayList<>();
		tests.add(new FastCSVLucIS());
		tests.add(new FastCSVFastBuf());
//		tests.add(new FastCSVJDKBuf());
//		tests.add(new OpenCSV_JDK());
		tests.add(new OpenCSVFastRead());
		tests.add(new OpenCSVLucRead());
		
		if ( ! csvFile.exists()) {
			OutputStream os = csvFile.createWritingStream();
			LongProcess lp = new LongProcess("creating " + csvFile, "line", nbLinesInFile);

			for (int i = 0; i < nbLinesInFile; ++i) {
				os.write(line);
				os.write('\n');
				lp.sensor.progressStatus++;
			}

			lp.end();
			os.close();
		}

		for (Test method : tests) {
			Function f = plot.createFunction(method.getName());

			for (int nbLines = nbLinesInFile; nbLines <= nbLinesInFile*100; nbLines *= 10) {
				Configuration config = f.configuration(nbLines + "");

				while (config.countMeasures() < 3) {
					long startDate = System.nanoTime();

					for (int i = 0; i < nbLines / nbLinesInFile; ++i) {
						method.f(nbLinesInFile, nbColumns);
					}

					config.addMeasure(nbLines, System.nanoTime() - startDate);
				}
			}
		}

		experiment.display();
	}

	private static String createLine(int n) {
		String line = "";

		for (int i = 0; i < n; ++i) {
			if (i > 0) {
				line += ",";
			}

			line += (int) (Math.random() * 100);
		}

		return line;
	}

	static class FastCSVLucIS implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = csvFile.createReadingStream(0);
			PagingInputStream pr = new PagingInputStream(in);

			for (long lineNumber = 0; lineNumber < nbLines; ++lineNumber) {
				for (int c = 0; c < nbColumns; ++c) {
					FastCSV.parseLong(pr, - 1);
				}
			}

			pr.close();

		}

		@Override
		public String getName() {
			return "FastCSV & PagingInputStream";
		}
	}

	static class FastCSVJDKBuf implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = csvFile.createReadingStream(1024 * 1024);

			for (long lineNumber = 0; lineNumber < nbLines; ++lineNumber) {
				for (int c = 0; c < nbColumns; ++c) {
					FastCSV.parseLong(in, - 1);
				}
			}

			in.close();
		}

		@Override
		public String getName() {
			return "FastCSV & BufferedInputStream";
		}
	}

	static class FastCSVFastBuf implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = csvFile.createReadingStream(0);
			FastBufferedInputStream pr = new FastBufferedInputStream(in);

			for (long lineNumber = 0; lineNumber < nbLines; ++lineNumber) {
				for (int c = 0; c < nbColumns; ++c) {
					FastCSV.parseLong(pr, - 1);
				}
			}

			pr.close();
		}

		@Override
		public String getName() {
			return "FastCSV & FastBufferedInputStream";
		}
	}

	static class OpenCSV_JDK implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = csvFile.createReadingStream();
			CSVReader reader = new CSVReader(new InputStreamReader(in));
			Iterator<String[]> iterator = reader.iterator();

			while (iterator.hasNext()) {
				String[] line = iterator.next();

				for (int c = 0; c < nbColumns; ++c) {
					Long.valueOf(line[c]);
				}
			}

			reader.close();
		}

		@Override
		public String getName() {
			return "OpenCSV & BufferedInputStream";
		}
	}

	static class OpenCSVFastRead implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = new FastBufferedInputStream(csvFile.createReadingStream());
			CSVReader reader = new CSVReader(new InputStreamReader(in));
			Iterator<String[]> iterator = reader.iterator();

			while (iterator.hasNext()) {
				String[] line = iterator.next();

				for (int c = 0; c < nbColumns; ++c) {
					Long.valueOf(line[c]);
				}
			}

			reader.close();

		}

		@Override
		public String getName() {
			return "OpenCSV & FastBufferedInputStream";
		}
	}
	
	static class OpenCSVLucRead implements Test {
		public void f(int nbLines, int nbColumns) throws IOException {
			InputStream in = new PagingInputStream(csvFile.createReadingStream());
			CSVReader reader = new CSVReader(new InputStreamReader(in));
			Iterator<String[]> iterator = reader.iterator();

			while (iterator.hasNext()) {
				String[] line = iterator.next();

				for (int c = 0; c < nbColumns; ++c) {
					Long.valueOf(line[c]);
				}
			}

			reader.close();

		}

		@Override
		public String getName() {
			return "OpenCSV & PagingInputStream";
		}
	}
}
