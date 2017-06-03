package generalpurpose;

public class gpLanguage {
	
	String[][]  arLanguage = {
			 {"afr","Afrikaans"}
			,{"amh","Amharic"}
			,{"ara","Arabic"}
			,{"asm","Assamese"}
			,{"aze","Azerbaijani"}
			,{"aze_cyrl","Azerbaijani-Cyrilic"}
			,{"bel","Belarusian"}
			,{"ben","Bengali"}
			,{"bod","Tibetan"}
			,{"bos","Bosnian"}
			,{"bul","Bulgarian"}
			,{"cat","Catalan"}
			,{"ceb","Cebuano"}
			,{"ces","Czech"}
			,{"chi_sim","Chinese-Simplified"}
			,{"chi_tra","Chinese-Traditional"}
			,{"chr","Cherokee"}
			,{"cym","Welsh"}
			,{"dan","Danish"}
			,{"dan_frak","Danish-Fraktur"}
			,{"deu","German"}
			,{"deu_frak","German-Fraktur"}
			,{"dzo","Dzongkha"}
			,{"ell","Greek-Modern"}
			,{"eng","English"}
			,{"epo","Esperanto"}
			,{"est","Estonian"}
			,{"eus","Basque"}
			,{"fas","Persian"}
			,{"fin","Finnish"}
			,{"fra","French"}
			,{"frk","Frankish"}
			,{"frm","French-Middle"}
			,{"gle","Irish"}
			,{"glg","Galician"}
			,{"grc","Greek-Ancient"}
			,{"guj","Gujarati"}
			,{"hat","Haitian-Creole"}
			,{"heb","Hebrew"}
			,{"hin","Hindi"}
			,{"hrv","Croatian"}
			,{"hun","Hungarian"}
			,{"iku","Inuktitut"}
			,{"ind","Indonesian"}
			,{"isl","Icelandic"}
			,{"ita","Italian"}
			,{"ita_old","Italian-Old"}
			,{"jav","Javanese"}
			,{"jpn","Japanese"}
			,{"kan","Kannada"}
			,{"kat","Georgian"}
			,{"kat_old","Georgian-Old"}
			,{"kaz","Kazakh"}
			,{"khm","Central-Khmer"}
			,{"kir","Kirghiz"}
			,{"kor","Korean"}
			,{"kur","Kurdish-Lao"}
			,{"lat","Latin"}
			,{"lav","Latvian"}
			,{"lit","Lithuanian"}
			,{"mal","Malayalam"}
			,{"mar","Marathi"}
			,{"mkd","Macedonian"}
			,{"mlt","Maltese"}
			,{"msa","Malay"}
			,{"mya","Burmese"}
			,{"nep","Nepali"}
			,{"nld","Dutch"}
			,{"nor","Norwegian"}
			,{"ori","Oriya"}
			,{"pan","Punjabi"}
			,{"pol","Polish"}
			,{"por","Portuguese"}
			,{"pus","Pashto"}
			,{"ron","Romanian-Moldavian"}
			,{"rus","Russian"}
			,{"san","Sanskrit"}
			,{"sin","Sinhala"}
			,{"slk","Slovak"}
			,{"slk_frak","Slovak-Fraktur"}
			,{"slv","Slovenian"}
			,{"spa","Spanish"}
			,{"sqi","Albanian"}
			,{"srp","Serbian"}
			,{"srp_latn","Serbian-Latin"}
			,{"swa","Swahili"}
			,{"swe","Swedish"}
			,{"syr","Syriac"}
			,{"tam","Tamil"}
			,{"tel","Telugu"}
			,{"tgk","Tajik"}
			,{"tgl","Tagalog"}
			,{"tha","Thai"}
			,{"tir","Tigrinya"}
			,{"tur","Turkish"}
			,{"uig","Uighur"}
			,{"ukr","Ukrainian"}
			,{"unk","Unknown"}
			,{"urd","Urdu"}
			,{"uzb","Uzbek"}
			,{"uzb_cyrl","Uzbek-Cyrilic"}
			,{"vie","Vietnamese"}
			,{"yid","Yiddish"}
	};
	
	//---------------------------------------------------------------------------------
	public gpLanguage()
	//---------------------------------------------------------------------------------
	{
		
	}

	//---------------------------------------------------------------------------------
	public String[] makeLanguageList()
	//---------------------------------------------------------------------------------
	{
		int naant = arLanguage.length;
		String[] languageList = new String[naant];
	    for(int i=0;i<naant;i++)
	    {
	    	languageList[i] = arLanguage[i][1];
	    }
		return languageList;    	
	}
	
	//---------------------------------------------------------------------------------
	public String getTesseractLanguageCode(String cmxLanguage) 
	//---------------------------------------------------------------------------------
	{
		int naant = arLanguage.length;
	    for(int i=0;i<naant;i++)
	    {
	    	if( arLanguage[i][1].compareToIgnoreCase(cmxLanguage) == 0 ) return arLanguage[i][0];
	    }	
		return "null";
	}
	
}
