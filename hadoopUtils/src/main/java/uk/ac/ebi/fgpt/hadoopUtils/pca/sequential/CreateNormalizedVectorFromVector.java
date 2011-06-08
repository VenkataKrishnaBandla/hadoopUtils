package uk.ac.ebi.fgpt.hadoopUtils.pca.sequential;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.WritableComparable;
import org.apache.mahout.math.VectorWritable;

import uk.ac.ebi.fgpt.hadoopUtils.pca.math.VectorOperations;

/**
 * This class takes in a Sequence File of Vectors and normalizes each vector.
 * 
 * @author vincent@ebi.ac.uk
 * 
 */
public class CreateNormalizedVectorFromVector extends SequentialTool {
  public static void main(String[] args) throws IOException {
    // Create Options
    Options cliOptions = new Options();
    Option input = OptionBuilder.withArgName("input.tsv").hasArg().isRequired().withDescription(
      "use given file as input").withLongOpt("input").create("i");
    Option output = OptionBuilder.withArgName("output.seqFile").hasArg().isRequired().withLongOpt("output")
        .withDescription("use given file as output").create("o");
    
    // Add Options
    cliOptions.addOption(input);
    cliOptions.addOption(output);
    
    HelpFormatter formatter = new HelpFormatter();
    
    // Try to parse options
    CommandLineParser parser = new PosixParser();
    
    if (args.length <= 1) {
      formatter.printHelp("normalizeVector", cliOptions, true);
      return;
    }
    
    try {
      CommandLine cmd = parser.parse(cliOptions, args, true);
      String pathToInput = cmd.getOptionValue("i");
      String pathToOutput = cmd.getOptionValue("o");
      
      run(pathToInput, pathToOutput);
      
    } catch (ParseException e) {
      formatter.printHelp("normalizeVector", cliOptions, true);
    }
  }
  
  public static void run(String stringToInput, String stringToOutput) throws IOException {
    setup(stringToInput, stringToOutput);
    
    SequenceFile.Reader reader = new SequenceFile.Reader(fs, inputPath, config);
    
    try {
      WritableComparable key = (WritableComparable<IntWritable>) reader.getKeyClass().newInstance();
      VectorWritable value = (VectorWritable) reader.getValueClass().newInstance();
      
      SequenceFile.Writer writer = SequenceFile.createWriter(fs, config, outputPath, IntWritable.class,
        VectorWritable.class);
      
      while (reader.next(key, value)) {
        writer.append(key, new VectorWritable(VectorOperations.normalize(value.get())));
      }
      writer.close();
      reader.close();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
