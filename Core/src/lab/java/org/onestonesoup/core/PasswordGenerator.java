package org.onestonesoup.core;

import org.onestonesoup.core.process.CommandLineTool;

public class PasswordGenerator extends CommandLineTool{

	public PasswordGenerator(String[] args) {
		super(args);
	}

	public static void main(String[] args) {
		new PasswordGenerator(args);
	}
	
	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 0;
	}

	private static final String[] CONSONANTS = {
		"b","br","bl",
		"c","cr","ch",
		"d","dr","dl",
		"f","fr","fl",
		"g","gr","gl",
		"h",
		"j",
		"k","kl",
		"l",
		"m",
		"n",
		"p","pr","pl",
		"qu",
		"r",
		"s","sh","sl","sn","sp","st",
		"t","th",
		"v","vl",
		"y"
	};
	private static final String[] VOWELS = {
		"a","ai",
		"e","ee",
		"i","ie",
		"o","oo","oa","oi",
		"u"
	};
	
	@Override
	public String getUsage() {
		return "<seed number>";
	}

	@Override
	public void process() {
		String password = "";
		
		for(int i=0;i<2;i++) {
			password+= CONSONANTS[ (int)(Math.random()*CONSONANTS.length) ];
			password+= VOWELS[ (int)(Math.random()*VOWELS.length) ];
		}
		
		password+= CONSONANTS[ (int)(Math.random()*CONSONANTS.length) ];
		
		System.out.println(password);
	}
}
