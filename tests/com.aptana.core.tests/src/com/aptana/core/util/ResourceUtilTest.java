/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.core.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;

import junit.framework.TestCase;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class ResourceUtilTest extends TestCase
{

	private static final String WEBNATUREID = "com.aptana.projects.webnature";
	private static final String RUBYNATUREID = "com.aptana.ruby.core.rubynature";
	private static final String RAILSNATUREID = "org.radrails.rails.core.railsnature";
	private static final String PHPNATUREID = "com.aptana.editor.php.phpNature";
	private static final String TITANIUM_MOBILE_NATURE_ID = "com.appcelerator.titanium.mobile.nature";
	private static final String TITANIUM_DESKTOP_NATURE_ID = "com.appcelerator.titanium.desktop.nature";
	private static final String JAVANATUREID = "org.eclipse.jdt.core.javanature";
	private static final String UnifiedBuilder_ID = "com.aptana.ide.core.unifiedBuilder";

	private IProject testProject;

	protected void setUp() throws Exception
	{
		testProject = createProject();
	}

	protected void tearDown() throws Exception
	{
		deleteProject(testProject);
	}

	public void testResourcePathToFile()
	{
		URL url = Platform.getBundle("com.aptana.core.tests").getEntry("resources");

		File file = ResourceUtil.resourcePathToFile(url);
		assertTrue(file.exists());
	}

	public void testResourcePathToFileWithNull()
	{
		assertNull(ResourceUtil.resourcePathToFile(null));
	}

	public void testResourcePathToString()
	{
		URL url = Platform.getBundle("com.aptana.core.tests").getEntry("resources");

		String path = ResourceUtil.resourcePathToString(url);
		String expectedPostfix = "/resources";
		assertTrue(MessageFormat.format("Path ({0}) doesn''t end with expected postfix: {1}", path, expectedPostfix),
				path.endsWith(expectedPostfix));
	}

	public void testResourcePathToStringWithNull()
	{
		assertNull(ResourceUtil.resourcePathToString(null));
	}

	public void testResourcePathToURI()
	{
		URL url = Platform.getBundle("com.aptana.core.tests").getEntry("resources");

		URI uri = ResourceUtil.resourcePathToURI(url);
		String expectedPostfix = "/resources/";
		assertTrue(MessageFormat.format("URI ({0}) doesn''t end with expected postfix: {1}", uri.toString(),
				expectedPostfix), uri.toString().endsWith(expectedPostfix));
	}

	public void testResourcePathToURIWithNull()
	{
		assertNull(ResourceUtil.resourcePathToURI(null));
	}

	public void testToURIWithSpaceInURL() throws Exception
	{
		String urlString = "file:/Applications/Aptana Studio 3/";

		assertEquals("file:/Applications/Aptana%20Studio%203/", ResourceUtil.toURI(new URL(urlString)).toString());
	}

	public void testToURIWithUNCPath() throws Exception
	{
		String urlString = "file://Server/Volume/File";

		assertEquals("file:////Server/Volume/File", ResourceUtil.toURI(new URL(urlString)).toString());
	}

	public void testToURIWithHttpProtocol() throws Exception
	{
		String urlString = "http://www.appcelerator.com";

		assertEquals(urlString, ResourceUtil.toURI(new URL(urlString)).toString());
	}

	public void testToURIWithNullURL() throws Exception
	{
		assertNull(ResourceUtil.toURI(null));
	}

	public void testGetLineSeparatorValue()
	{
		assertEquals(System.getProperty("line.separator"), ResourceUtil.getLineSeparatorValue(null));
		assertEquals(System.getProperty("line.separator"), ResourceUtil.getLineSeparatorValue(testProject));
	}

	public void testBuilders() throws CoreException
	{
		IProjectDescription desc = testProject.getDescription();
		int numCommands = desc.getBuildSpec().length;

		ResourceUtil.addBuilder(testProject, UnifiedBuilder_ID);

		// get new value
		desc = testProject.getDescription();

		boolean foundBuilder = false;
		ICommand[] commands = desc.getBuildSpec();
		for (ICommand command : commands)
		{
			if (UnifiedBuilder_ID.equals(command.getBuilderName()))
			{
				foundBuilder = true;
				break;
			}
		}

		// That there is the builder in question;
		assertTrue(foundBuilder);

		ResourceUtil.removeBuilder(testProject, UnifiedBuilder_ID);

		// get new value
		desc = testProject.getDescription();

		// make sure we removed a builder
		assertEquals(numCommands, desc.getBuildSpec().length);

		foundBuilder = false;
		commands = desc.getBuildSpec();
		for (ICommand command : commands)
		{
			if (UnifiedBuilder_ID.equals(command.getBuilderName()))
			{
				foundBuilder = true;
				break;
			}
		}

		// That there is _not_ the builder in question;
		assertFalse(foundBuilder);

		// re-add builder
		ResourceUtil.addBuilder(testProject, UnifiedBuilder_ID);

		desc = testProject.getDescription();

		// now add two Aptana natures
		ResourceUtil.addNature(testProject, WEBNATUREID);

		// try to remove the builder. It should not
		assertFalse(ResourceUtil.removeBuilderIfOrphaned(testProject, UnifiedBuilder_ID));

		// remove a nature. Internally will remove builder if orphaned
		ResourceUtil.removeNature(testProject, WEBNATUREID);

		// back to original builder length
		assertEquals(numCommands, testProject.getDescription().getBuildSpec().length);
	}

	public void testIsAptanaNature()
	{
		assertTrue(ResourceUtil.isAptanaNature(WEBNATUREID));
		assertTrue(ResourceUtil.isAptanaNature(RUBYNATUREID));
		assertTrue(ResourceUtil.isAptanaNature(RAILSNATUREID));
		assertTrue(ResourceUtil.isAptanaNature(PHPNATUREID));
		assertTrue(ResourceUtil.isAptanaNature(TITANIUM_MOBILE_NATURE_ID));
		assertTrue(ResourceUtil.isAptanaNature(TITANIUM_DESKTOP_NATURE_ID));
		assertFalse(ResourceUtil.isAptanaNature(JAVANATUREID));
	}

	public void testNatures() throws CoreException
	{
		IProjectDescription desc = testProject.getDescription();

		int numIds = desc.getNatureIds().length;

		ResourceUtil.addNature(desc, WEBNATUREID);
		ResourceUtil.addNature(desc, RUBYNATUREID);
		String[] natures = ResourceUtil.getAptanaNatures(desc);

		// make sure we added two ids
		assertEquals(numIds + 2, desc.getNatureIds().length);

		// That there are two aptana natures
		assertEquals(2, natures.length);
		assertEquals(WEBNATUREID, natures[0]);
		assertEquals(RUBYNATUREID, natures[1]);

		ResourceUtil.removeNature(desc, WEBNATUREID);
		ResourceUtil.removeNature(desc, RUBYNATUREID);

		// make sure we removed two ids
		assertEquals(numIds, desc.getNatureIds().length);
		natures = ResourceUtil.getAptanaNatures(desc);
		assertEquals(0, natures.length);
	}

	/**
	 * Creates a project for testing
	 * 
	 * @return
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 * @throws CoreException
	 */
	private IProject createProject() throws IOException, InvocationTargetException, InterruptedException, CoreException
	{

		File baseTempFile = File.createTempFile("test", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
		String projectName = "ResourceUtilTest" + System.currentTimeMillis();
		File projectFolder = new File(baseTempFile.getParentFile(), projectName);
		projectFolder.mkdirs();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(projectName);
		description.setLocation(new Path(projectFolder.getAbsolutePath()));

		IProject project = workspace.getRoot().getProject(projectName);
		project.create(description, null);
		project.open(null);

		return project;
	}

	/**
	 * Deletes a project used for testing
	 * 
	 * @param project
	 * @throws CoreException
	 */
	private void deleteProject(IProject project) throws CoreException
	{
		project.delete(true, null);
	}
}
