package org.onestonesoup.core.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Created by nikcross on 26/06/17.
 */
public class JsonHelperTest {

	@Test
	public void shouldParseSimpleJson() {

		//Given
		String data = "{ a: \"1\", b: 2, \"c\": 3, \"d\": \"4\"}";

		//When
		EntityTree thing = JsonHelper.parseObject("name",data);

		//Then

		assertThat(thing.getChild("a").getValue(),is("1"));
		assertThat(thing.getChild("b").getValue(),is("2"));
		assertThat(thing.getChild("c").getValue(),is("3"));
		assertThat(thing.getChild("d").getValue(),is("4"));
	}

	@Test
	public void shouldParseComplexJson() {

		//Given
		String data = "{ a: \"1\", b: { ab: 12, cd: \"34\", \"de\": { def: \"g\"} }, \"c\": 3, \"d\": \"4\"}";

		//When
		EntityTree thing = JsonHelper.parseObject("name",data);

		//Then

		assertThat(thing.getChild("a").getValue(),is("1"));
		assertThat(thing.getChild("d").getValue(),is("4"));
		assertThat(thing.getChild("b").getChild("ab").getValue(),is("12"));
		assertThat(thing.getChild("b").getChild("cd").getValue(),is("34"));
		assertThat(thing.getChild("b").getChild("de").getChild("def").getValue(),is("g"));
	}

	@Test
	public void shouldParseJsonArray() {

		//Given
		String data = "{ a: \"1\", b: [\"ba\", \"bb\", \"bc\"], \"c\": 3, \"d\": \"4\"}";

		//When
		EntityTree thing = JsonHelper.parseObject("name",data);

		//Then

		assertThat(thing.getChild("a").getValue(),is("1"));
		assertThat(thing.getChild("d").getValue(),is("4"));
		assertThat(thing.getChild("b").getAttribute("array"),is("true"));
		assertThat(thing.getChild("b").getAttribute("length"),is("3"));
		assertThat(thing.getChild("b").getChild("0").getValue(),is("ba"));
		assertThat(thing.getChild("b").getChild("1").getValue(),is("bb"));
		assertThat(thing.getChild("b").getChild("2").getValue(),is("bc"));
	}

	@Test
	public void shouldStringifySimpleJson() {
		//Given
		EntityTree entity = new EntityTree("test");
		entity.addChild("a").setValue("1");
		entity.addChild("b").setValue("2");
		entity.addChild("c").setValue("3");
		entity.addChild("d").setValue("4");

		//When
		String data = JsonHelper.stringifyObject(entity.getRoot());

		//Then
		assertThat(data,is("\n{\"a\": \"1\",\"b\": \"2\",\"c\": \"3\",\"d\": \"4\"}\n"));
	}

	@Test
	public void shouldStringifyComplexJson() {
		//Given
		EntityTree entity = new EntityTree("test");
		entity.addChild("a").setValue("1");

		EntityTree.TreeEntity b = entity.addChild("b");
		b.addChild("ab").setValue("12");
		b.addChild("cd").setValue("34");
		b.addChild("de").addChild("def").setValue("g");

		entity.addChild("c").setValue("3");
		entity.addChild("d").setValue("4");

		//When
		String data = JsonHelper.stringifyObject(entity.getRoot());

		//Then
		assertThat(data,is("\n{\"a\": \"1\",\"b\": \n{\"ab\": \"12\",\"cd\": \"34\",\"de\": \n{\"def\": \"g\"}\n}\n,\"c\": \"3\",\"d\": \"4\"}\n"));
	}

	@Test
	public void shouldDoThis() {
		String data = "{\"type\":\"command\",\"data\":{\"action\":\"connectNodes\",\"network\":\"testNetwork\",\"connectFromNode\":{\"x\":0,\"y\":0,\"z\":0,\"weight\":1},\"node\":{\"x\":1,\"y\":0,\"z\":0}}}";

		JsonHelper.parseObject("packet",data);
	}
}
