package edu.iu.indra.scigw.config;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.iu.indra.scigw.Connector;
import edu.iu.indra.scigw.ScpHandler;
import edu.iu.indra.scigw.util.Constants;

public class ApplicationFileTransferHelper
{
	final static Logger logger = Logger.getLogger(ApplicationFileTransferHelper.class);

	/**
	 * transfers job files to server and returns destination path of pbs script
	 * for scheduling
	 * 
	 * @param config
	 * @return PBS script path on server
	 */
	public static String transferApplicationFiles(JobConfig config)
	{
		logger.info("Transferring files to server");

		String destPbsScriptPath = "";

		String pbsScriptPath = config.getPbsScriptPath();

		if (pbsScriptPath == null || pbsScriptPath.length() <= 1)
		{
			throw new RuntimeException("PBS script path must be set in job config");
		}

		// get unique ID of job
		String uid = config.getUid().toString();

		try
		{
			Connector connector = Connector.getInstance();

			// create a new directory for job
			String dirPath = Constants.getJobDirPath(uid);
			connector.executeCommands("mkdir " + dirPath);

			destPbsScriptPath = dirPath + "pbs.sh";
			// transfer pbs script to the directory
			ScpHandler.copyJobFilesToHost(destPbsScriptPath, pbsScriptPath);

			logger.info("PBS file transfer complete");

			// transfer input files one by one
			Map<String, String> inputFiles = config.getInputFiles();

			if (inputFiles != null)
			{
				for (String sourceFile : inputFiles.keySet())
				{
					logger.info("Transferring file " + sourceFile + " to server");
					ScpHandler.copyJobFilesToHost(inputFiles.get(sourceFile), sourceFile);
					Thread.sleep(1000);
				}
			}

			// file transfer complete
			logger.info("File transfer complete");

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return destPbsScriptPath;
	}

}