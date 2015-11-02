package fr.frogdevelopment.nihongo.kana;


import fr.frogdevelopment.nihongo.R;

public enum Katakana {

	A("ア", R.drawable.hiragana_a),
	I("イ", R.drawable.hiragana_i),
	U("ウ", R.drawable.hiragana_u),
	E("エ", R.drawable.hiragana_e),
	O("オ", R.drawable.hiragana_o),
	KA("カ", R.drawable.hiragana_ka),
	KI("キ", R.drawable.hiragana_ki),
	KU("ク", R.drawable.hiragana_ku),
	KE("ケ", R.drawable.hiragana_ke),
	KO("コ", R.drawable.hiragana_ko),
	TA("タ", R.drawable.hiragana_ta),
	CHI("チ", R.drawable.hiragana_chi),
	TSU("ツ", R.drawable.hiragana_tsu),
	TE("テ", R.drawable.hiragana_te),
	TO("ト", R.drawable.hiragana_to),
	NA("ナ", R.drawable.hiragana_na),
	NI("ニ", R.drawable.hiragana_ni),
	NU("ヌ", R.drawable.hiragana_nu),
	NE("ネ", R.drawable.hiragana_ne),
	NO("ノ", R.drawable.hiragana_no),
	HA("ハ", R.drawable.hiragana_ha),
	HI("ヒ", R.drawable.hiragana_hi),
	FU("フ", R.drawable.hiragana_fu),
	HE("ヘ", R.drawable.hiragana_he),
	HO("ホ", R.drawable.hiragana_ho),
	MA("マ", R.drawable.hiragana_ma),
	MI("ミ", R.drawable.hiragana_mi),
	MU("ム", R.drawable.hiragana_mu),
	ME("メ", R.drawable.hiragana_me),
	MO("モ", R.drawable.hiragana_mo),
	YA("ヤ", R.drawable.hiragana_ya),
	YU("ユ", R.drawable.hiragana_yu),
	YO("ヨ", R.drawable.hiragana_yo),
	RA("ラ", R.drawable.hiragana_ra),
	RI("リ", R.drawable.hiragana_ri),
	RU("ル", R.drawable.hiragana_ru),
	RE("レ", R.drawable.hiragana_re),
	RO("ロ", R.drawable.hiragana_ro),
	WA("ワ", R.drawable.hiragana_wa),
	WO("ヲ", R.drawable.hiragana_wo),
	N("ン", R.drawable.hiragana_n);

	public final String label;
	public final int    resource;

	Katakana(String label, int resource) {
		this.label = label;
		this.resource = resource;
	}

}
