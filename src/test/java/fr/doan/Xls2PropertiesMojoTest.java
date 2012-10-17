package fr.doan;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

public class Xls2PropertiesMojoTest
{

	private Xls2PropertiesMojo plugin = new Xls2PropertiesMojo();
	private Workbook excel;

	@Before
	public void init() throws Exception
	{
		File xlsFile = new File("src/test/resources/sample.xlsx");
		assertThat(xlsFile.exists()).isTrue();

		ReflectionUtils.setField(plugin, "xlsFile", xlsFile);

		try
		{
			FileInputStream input = new FileInputStream(xlsFile);
			excel = new XSSFWorkbook(input);
		}
		catch (IOException ioe)
		{
			throw new Exception("XLS file could not be open", ioe);
		}
	}

	@Test
	public void should_retrieve_parameters() throws Exception
	{

		ReflectionUtils.invoke(this.plugin, "initProperties", excel.getSheet("SETTINGS"));

		String defaultLocale = (String) ReflectionUtils.getField(plugin, "defaultLocale");
		String resourceFileName = (String) ReflectionUtils.getField(plugin, "resourceFileName");

		assertThat(defaultLocale).isEqualTo("en_GB");
		assertThat(resourceFileName).isEqualTo("messages");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void should_load_resources_in_a_sheet() throws Exception
	{
		Map<String, Map<String, String>> resourcesMap = new HashMap<String, Map<String, String>>();
		resourcesMap.put("DEFAULT", new HashMap<String, String>());
		ReflectionUtils.setField(plugin, "resourcesMap", resourcesMap);
		ReflectionUtils.invoke(this.plugin, "loadResources", excel.getSheet("DEFAULT"));
		resourcesMap = (Map<String, Map<String, String>>) ReflectionUtils.getField(plugin, "resourcesMap");

		assertThat(resourcesMap.entrySet().size()).isEqualTo(1);

		Map<String, String> resources = resourcesMap.get("DEFAULT");
		assertThat(resources.get("aaa")).isEqualTo("default aaa");
		assertThat(resources.get("bbb")).isEqualTo("default bbb");
		assertThat(resources.get("ccc")).isEqualTo("default ccc");
		assertThat(resources.get("ddd")).isEqualTo("default ddd");
		assertThat(resources.get("eee")).isEqualTo("default eee");
		assertThat(resources.get("fff")).isEqualTo("default fff");
	}

	@Test
	public void should_merge_resources_with_defaults() throws Exception
	{
		Map<String, String> defaults = new HashMap<String, String>();
		Map<String, String> resources = new HashMap<String, String>();

		defaults.put("aaa", "default aaa");
		defaults.put("bbb", "default bbb");
		defaults.put("ccc", "default ccc");
		defaults.put("ddd", "default ddd");

		resources.put("aaa", "custom aaa");
		resources.put("eee", "custom eee");
		resources.put("bbb", "custom bbb");

		ReflectionUtils.invoke(this.plugin, "mergeResourcesWithDefault", defaults, resources);

		assertThat(resources.entrySet().size()).isEqualTo(5);
		assertThat(resources.get("aaa")).isEqualTo("custom aaa");
		assertThat(resources.get("bbb")).isEqualTo("custom bbb");
		assertThat(resources.get("ccc")).isEqualTo("default ccc");
		assertThat(resources.get("ddd")).isEqualTo("default ddd");
		assertThat(resources.get("eee")).isEqualTo("custom eee");
	}

	@Test
	public void should_execute() throws Exception
	{
		ReflectionUtils.setField(plugin, "basedir", System.getProperty("user.dir"));
		this.plugin.execute();

		File defaults = new File("./target/resources/messages_en_GB.properties");
		File fr = new File("./target/resources/messages_fr_FR.properties");
		File us = new File("./target/resources/messages_en_US.properties");
		File es = new File("./target/resources/messages_es_ES.properties");

		assertThat(defaults.exists()).isTrue();
		assertThat(fr.exists()).isTrue();
		assertThat(us.exists()).isTrue();
		assertThat(es.exists()).isTrue();
	}
}
