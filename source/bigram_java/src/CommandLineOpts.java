import com.beust.jcommander.Parameter;

public class CommandLineOpts {
	@Parameter(names = { "-trainTagged" }, description = "sets tagged training file[s] (can be single file or file with list of files)")
	public String taggedTrainFile;

	@Parameter(names = { "-trainSingle" }, description = "only train a single file")
	public boolean trainSingle = false;
	@Parameter(names = { "-testSingle" }, description = "only tests a single file")
	public boolean testSingle = false;

	@Parameter(names = { "-testTagged"},         description = "sets tagged testing file[s] (single file with list of files)")
	public String taggedTestFile;

	@Parameter(names = { "-languageNum"},  description = "The language number for the test file.")
	public Integer languageNum = 0;

	@Parameter(names = { "-k"},            description = "Number of top bigrams/unigrams to print")
	public Integer K = 200;

	@Parameter(names = { "-h", "-help"},    description = "Prints usage and exits")
	public boolean help = false;

	@Parameter(names = { "-v", "-verbose"}, description = "Print out debug information")
	public boolean verbose = false;
}
