package fr.doan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Goal which creates as many properties files as there are environments declared in the XLS file
 * 
 * @goal generate
 * @phase compile
 */

public class Xls2PropertiesMojo extends AbstractMojo
{

	private static final String SETTINGS_SHEET = "SETTINGS";
	private static final String DEFAULT_SHEET = "DEFAULT";
	private static final String DEFAULT_LOCALE = "en_GB";
	private static final String DEFAULT_RESOURCE_FILE_NAME = "messages";
	private final StringComparator COMPARATOR = new StringComparator();
	/**
	 * Location of the XLS file.
	 * 
	 * @parameter
	 * @required
	 */
	private File xlsFile;

	/**
	 * 
	 * @parameter expression="${project.basedir}"
	 */
	private String basedir = "";

	/**
	 * Do we allow to have property not defined? By default, no, and if a property is not set on an environment, an error is raised.
	 * 
	 * @parameter
	 */
	private boolean allowMissingProperty = false;

	/**
	 * Locale for the DEFAULT values sheet
	 */
	private String defaultLocale;

	/**
	 * File name prefix for generated resource files
	 */
	private String resourceFileName;

	/**
	 * Map containing all resources by locale
	 */
	private Map<String, Map<String, String>> resourcesMap = new TreeMap<String, Map<String, String>>(COMPARATOR);

	private HSSFDataFormatter formatter = new HSSFDataFormatter();

	public void execute() throws MojoExecutionException
	{
		// Open the Excel file
		Workbook excel = null;
		boolean isOldExcel = StringUtils.endsWithIgnoreCase(xlsFile.getName(), ".xls");
		try
		{
			FileInputStream input = new FileInputStream(xlsFile);
			if (isOldExcel)
			{
				excel = new HSSFWorkbook(input);
			}
			else
			{
				excel = new XSSFWorkbook(input);
			}
		}
		catch (IOException ioe)
		{
			throw new MojoExecutionException("XLS file could not be open", ioe);
		}

		if (excel.getNumberOfSheets() < 2)
		{
			throw new MojoExecutionException(
					"There should be at least 2 sheets in the XLS file, one for SETTINGS and one for DEFAULT resources values");
		}

		if (excel.getSheet(SETTINGS_SHEET) == null)
		{
			throw new MojoExecutionException("There should be a " + SETTINGS_SHEET + " sheet in the XLS file");
		}

		if (excel.getSheet(DEFAULT_SHEET) == null)
		{
			throw new MojoExecutionException("There should be a " + DEFAULT_SHEET + " sheet in the XLS file");
		}

		this.initProperties(excel.getSheetAt(0));
		for (int i = 1; i < excel.getNumberOfSheets(); i++)
		{
			loadResources(excel.getSheetAt(i));
		}

		this.createResourcesFiles();
	}

	private void initProperties(Sheet sheet)
	{
		// Retrieve default locale
		this.defaultLocale = sheet.getRow(2).getCell(2).getStringCellValue();
		if (StringUtils.isBlank(defaultLocale))
		{
			this.defaultLocale = DEFAULT_LOCALE;
		}

		// Retrieve resource file name
		this.resourceFileName = sheet.getRow(3).getCell(2).getStringCellValue();
		if (StringUtils.isBlank(defaultLocale))
		{
			this.resourceFileName = DEFAULT_RESOURCE_FILE_NAME;
		}
	}

	private void loadResources(Sheet sheet) throws MojoExecutionException
	{
		String locale = sheet.getSheetName();
		getLog().info("Reading Sheet '" + locale + "'");

		int rowCount = sheet.getLastRowNum() + 1;

		if (rowCount > 1)
		{
			Map<String, String> resources = new TreeMap<String, String>(COMPARATOR);
			for (int i = 1; i < rowCount; i++)
			{
				// Read all resources in this sheet
				resources.put(sheet.getRow(i).getCell(0).getStringCellValue().trim(), sheet.getRow(i).getCell(1).getStringCellValue().trim());
			}

			// If not DEFAULT sheet, merge resoruces with DEFAULT values
			if (!StringUtils.equals(locale, DEFAULT_SHEET))
			{
				Map<String, String> defaultResources = this.resourcesMap.get(DEFAULT_SHEET);
				if (defaultResources == null)
				{
					throw new MojoExecutionException("Cannot find " + DEFAULT_SHEET + " for resources merge");
				}
				this.mergeResourcesWithDefault(defaultResources, resources);
			}

			this.resourcesMap.put(locale, resources);
		}
		else
		{
			this.resourcesMap.put(locale, this.resourcesMap.get(DEFAULT_SHEET));
		}

	}

	private void mergeResourcesWithDefault(Map<String, String> defaultResources, Map<String, String> resources)
	{
		for (Entry<String, String> entry : defaultResources.entrySet())
		{
			if (!resources.containsKey(entry.getKey()))
			{
				resources.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private void createResourcesFiles()
	{
		if (!basedir.endsWith(File.separator))
		{
			basedir = basedir + File.separator;
		}
		File outputDirectory = new File(basedir + "target/resources");
		// Create 1 file per environment (column)
		if (!outputDirectory.exists())
		{
			outputDirectory.mkdirs();
		}
		for (Entry<String, Map<String, String>> localeEntry : resourcesMap.entrySet())
		{
			String locale = localeEntry.getKey();
			if (StringUtils.equals(locale, DEFAULT_SHEET))
			{
				locale = this.defaultLocale;
			}

			File file = new File(outputDirectory + "/" + resourceFileName + "_" + locale + ".properties");
			StringBuilder resources = new StringBuilder(60);
			for (Entry<String, String> resourceEntry : localeEntry.getValue().entrySet())
			{
				resources.append(resourceEntry.getKey());
				resources.append('=');
				resources.append(resourceEntry.getValue());
				resources.append('\n');

			}

			try
			{
				FileUtils.writeStringToFile(file, resources.toString());
				getLog().info("\tProperties file '" + file.getAbsolutePath() + "' created.");
			}
			catch (IOException e)
			{
				getLog().error("Could not write properties file '" + file.getAbsolutePath() + "'", e);
			}
		}
	}

	public class StringComparator implements Comparator<String>
	{

		public int compare(String arg0, String arg1)
		{
			if (!StringUtils.isBlank(arg0) && StringUtils.isBlank(arg1))
			{
				return 1;
			}
			else if (StringUtils.isBlank(arg0) && !StringUtils.isBlank(arg1))
			{
				return -1;
			}
			else if (StringUtils.isBlank(arg0) && StringUtils.isBlank(arg1))
			{
				return 0;
			}
			else
			{
				return arg0.compareTo(arg1);
			}

		}

	}
}