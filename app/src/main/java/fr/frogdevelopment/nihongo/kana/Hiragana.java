package fr.frogdevelopment.nihongo.kana;


import fr.frogdevelopment.nihongo.R;

public enum Hiragana {

	A("あ", R.drawable.hiragana_a, 0, 4),
	I("い", R.drawable.hiragana_i, 0, 3),
	U("う", R.drawable.hiragana_u, 0, 2),
	E("え", R.drawable.hiragana_e, 0, 1),
	O("お", R.drawable.hiragana_o, 0, 0),
	KA("か", R.drawable.hiragana_ka, 1, 4),
	KI("き", R.drawable.hiragana_ki, 1, 3),
	KU("く", R.drawable.hiragana_ku, 1, 2),
	KE("け", R.drawable.hiragana_ke, 1, 1),
	KO("こ", R.drawable.hiragana_ko, 1, 0),
	SA("さ", R.drawable.hiragana_sa, 2, 4),
	SHI("し", R.drawable.hiragana_shi, 2, 3),
	SU("す", R.drawable.hiragana_su, 2, 2),
	SE("せ", R.drawable.hiragana_se, 2, 1),
	SO("そ", R.drawable.hiragana_so, 2, 0),
	TA("た", R.drawable.hiragana_ta, 3, 4),
	CHI("ち", R.drawable.hiragana_chi, 3, 3),
	TSU("つ", R.drawable.hiragana_tsu, 3, 2),
	TE("て", R.drawable.hiragana_te, 3, 1),
	TO("と", R.drawable.hiragana_to, 3, 0),
	NA("な", R.drawable.hiragana_na, 4, 4),
	NI("に", R.drawable.hiragana_ni, 4, 3),
	NU("ぬ", R.drawable.hiragana_nu, 4, 2),
	NE("ね", R.drawable.hiragana_ne, 4, 1),
	NO("の", R.drawable.hiragana_no, 4, 0),
	HA("は", R.drawable.hiragana_ha, 5, 4),
	HI("ひ", R.drawable.hiragana_hi, 5, 3),
	FU("ふ", R.drawable.hiragana_fu, 5, 2),
	HE("へ", R.drawable.hiragana_he, 5, 1),
	HO("ほ", R.drawable.hiragana_ho, 5, 0),
	MA("ま", R.drawable.hiragana_ma, 6, 4),
	MI("み", R.drawable.hiragana_mi, 6, 3),
	MU("む", R.drawable.hiragana_mu, 6, 2),
	ME("め", R.drawable.hiragana_me, 6, 1),
	MO("も", R.drawable.hiragana_mo, 6, 0),
	YA("や", R.drawable.hiragana_ya, 7, 4),
	YU("ゆ", R.drawable.hiragana_yu, 7, 2),
	YO("よ", R.drawable.hiragana_yo, 7, 0),
	RA("ら", R.drawable.hiragana_ra, 8, 4),
	RI("り", R.drawable.hiragana_ri, 8, 3),
	RU("る", R.drawable.hiragana_ru, 8, 2),
	RE("れ", R.drawable.hiragana_re, 8, 1),
	RO("ろ", R.drawable.hiragana_ro, 8, 0),
	WA("わ", R.drawable.hiragana_wa, 9, 4),
	WO("を", R.drawable.hiragana_wo, 9, 0),
	N("ん", R.drawable.hiragana_n, 10, 4);

	public final String label;
	public final int    resource;
	public final int    col;
	public final int    row;

	Hiragana(String label, int resource, int row, int col) {
		this.label = label;
		this.resource = resource;
		this.col = col;
		this.row = row;
	}

}
