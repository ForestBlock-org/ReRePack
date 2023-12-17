package org.crayne.rerepack.util.minecraft;

import org.crayne.rerepack.util.string.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LanguageFiles {

    private LanguageFiles() {}

    @NotNull
    private static final Set<String> LANGUAGE_FILES = Set.of(
            "af_za", "cs_cz", "en_pt", "es_ve", "ga_ie", "isv", "li_li", "nl_nl", "se_no",
            "tl_ph", "zh_tw", "ar_sa", "cy_gb", "en_ud", "et_ee", "gd_gb", "it_it", "lmo",
            "nn_no", "sk_sk", "tok", "zlm_arab", "ast_es", "da_dk", "en_us", "eu_es", "gl_es",
            "ja_jp", "lol_us", "no_no", "sl_si", "tr_tr", "az_az", "de_at", "enws", "fa_ir",
            "haw_us", "jbo_en", "lt_lt", "oc_fr", "so_so", "tt_ru", "bar", "de_ch", "eo_uy",
            "fi_fi", "he_il", "ka_ge", "lv_lv", "ovd", "sq_al", "uk_ua", "ba_ru", "de_de",
            "esan", "fil_ph", "hi_in", "kk_kz", "lzh", "pl_pl", "sr_sp", "val_es", "be_by",
            "el_gr", "es_ar", "fo_fo", "hr_hr", "kn_in", "mk_mk", "pt_br", "sv_se", "vec_it",
            "bg_bg", "en_au", "es_cl", "fra_de", "hu_hu", "ko_kr", "mn_mn", "pt_pt", "sxu",
            "vi_vn", "brb", "en_ca", "es_ec", "fr_ca", "hy_am", "ksh", "ms_my", "qya_aa",
            "szl", "yi_de", "br_fr", "en_gb", "es_es", "fr_fr", "id_id", "kw_gb", "mt_mt",
            "ro_ro", "ta_in", "yo_ng", "bs_ba", "en_nz", "es_mx", "fur_it", "ig_ng", "la_la",
            "nds_de", "rpr", "th_th", "zh_cn"
    );

    @NotNull
    public static String withoutJsonExtension(@NotNull final String pattern) {
        if (pattern.endsWith(".json")) return pattern.substring(0, pattern.length() - ".json".length());

        return pattern;
    }

    @NotNull
    public static Set<String> allMatching(@NotNull final String pattern) {
        return StringUtil.allMatching(LANGUAGE_FILES, withoutJsonExtension(pattern));
    }

}
