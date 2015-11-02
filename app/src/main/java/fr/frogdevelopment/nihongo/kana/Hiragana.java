package fr.frogdevelopment.nihongo.kana;


import fr.frogdevelopment.nihongo.R;

public enum Hiragana {

	A("あ", R.drawable.hiragana_a),
	I("い", R.drawable.hiragana_i),
	U("う", R.drawable.hiragana_u),
	E("え", R.drawable.hiragana_e),
	O("お", R.drawable.hiragana_o),
	KA("か", R.drawable.hiragana_ka),
	KI("き", R.drawable.hiragana_ki),
	KU("く", R.drawable.hiragana_ku),
	KE("け", R.drawable.hiragana_ke),
	KO("こ", R.drawable.hiragana_ko),
	TA("た", R.drawable.hiragana_ta),
	CHI("ち", R.drawable.hiragana_chi),
	TSU("つ", R.drawable.hiragana_tsu),
	TE("て", R.drawable.hiragana_te),
	TO("と", R.drawable.hiragana_to),
	NA("な", R.drawable.hiragana_na),
	NI("に", R.drawable.hiragana_ni),
	NU("ぬ", R.drawable.hiragana_nu),
	NE("ね", R.drawable.hiragana_ne),
	NO("の", R.drawable.hiragana_no),
	HA("は", R.drawable.hiragana_ha),
	HI("ひ", R.drawable.hiragana_hi),
	FU("ふ", R.drawable.hiragana_fu),
	HE("へ", R.drawable.hiragana_he),
	HO("ほ", R.drawable.hiragana_ho),
	MA("ま", R.drawable.hiragana_ma),
	MI("み", R.drawable.hiragana_mi),
	MU("む", R.drawable.hiragana_mu),
	ME("め", R.drawable.hiragana_me),
	MO("も", R.drawable.hiragana_mo),
	YA("や", R.drawable.hiragana_ya),
	YU("ゆ", R.drawable.hiragana_yu),
	YO("よ", R.drawable.hiragana_yo),
	RA("ら", R.drawable.hiragana_ra),
	RI("り", R.drawable.hiragana_ri),
	RU("る", R.drawable.hiragana_ru),
	RE("れ", R.drawable.hiragana_re),
	RO("ろ", R.drawable.hiragana_ro),
	WA("わ", R.drawable.hiragana_wa),
	WO("を", R.drawable.hiragana_wo),
	N("ん", R.drawable.hiragana_n);

	public final String label;
	public final int    resource;

	Hiragana(String label, int resource) {
		this.label = label;
		this.resource = resource;
	}

}
