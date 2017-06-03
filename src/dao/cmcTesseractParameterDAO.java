package dao;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import generalpurpose.gpPrintStream;
import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;
import ocr.cmcTesseractParameter;

public class cmcTesseractParameterDAO {

	cmcProcSettings xMSet = null;
	logLiason logger=null;
	
	String[][] lijst = {
			
			{"F" , "allow_blob_division","1","Use divisible blobs chopping"},
			{"F" , "ambigs_debug_level","0","Debug level for unichar ambiguities"},
			{"F" , "applybox_debug","1","Debug level"},
			{"F" , "applybox_exposure_pattern","exp","Exposure value follows this pattern in the image filename. The name of the image files are expected to be in the form [lang].[fontname].exp[num].tif"},
			{"F" , "applybox_learn_chars_and_char_frags_mode","0","Learn both character fragments (as is done in the special low exposure mode) as well as unfragmented characters."},
			{"F" , "applybox_learn_ngrams_mode","0","Each bounding box is assumed to contain ngrams. Only learn the ngrams whose outlines overlap horizontally."},
			{"F" , "applybox_page","0","Page number to apply boxes from"},
			{"F" , "assume_fixed_pitch_char_segment","0","include fixed-pitch heuristics in char segmentation"},
			{"F" , "bestrate_pruning_factor","2","Multiplying factor of current best rate to prune other hypotheses"},
			{"F" , "bidi_debug","0","Debug level for BiDi"},
			{"F" , "bland_unrej","0","unrej potential with no checks"},
			{"F" , "certainty_scale","20","Certainty scaling factor"},
			{"F" , "certainty_scale","20","Certainty scaling factor"},
			{"F" , "chop_center_knob","0.15 ","Split center adjustment"},
			{"F" , "chop_centered_maxwidth","90","Width of (smaller) chopped blobs above which we don't care that a chop is not near the center."},
			{"F" , "chop_debug","0","Chop debug"},
			{"F" , "chop_enable","1","Chop enable"},
			{"F" , "chop_good_split","50","Good split limit"},
			{"F" , "chop_inside_angle","-50","Min Inside Angle Bend"},
			{"F" , "chop_min_outline_area","2000","Min Outline Area"},
			{"F" , "chop_min_outline_points","6","Min Number of Points on Outline"},
			{"F" , "chop_new_seam_pile","1","Use new seam_pile"},
			{"F" , "chop_ok_split","100","OK split limit"},
			{"F" , "chop_overlap_knob","0.9","Split overlap adjustment"},
			{"F" , "chop_same_distance","2","Same distance"},
			{"F" , "chop_seam_pile_size","150","Max number of seams in seam_pile"},
			{"F" , "chop_sharpness_knob","0.06","Split sharpness adjustment"},
			{"F" , "chop_split_dist_knob","0.5","Split length adjustment"},
			{"F" , "chop_split_length","10000","Split Length"},
			{"F" , "chop_vertical_creep","0","Vertical creep"},
			{"F" , "chop_width_change_knob","5","Width change adjustment"},
			{"F" , "chop_x_y_weight","3","X / Ylength weight"},
			{"F" , "chs_leading_punct","('`","Leading punctuation"},
			{"F" , "chs_trailing_punct1",").",";:?! 1st Trailing punctuation"},
			{"F" , "chs_trailing_punct2",")'`","2nd Trailing punctuation"},
			{"F" , "classify_adapt_feature_threshold","230","Threshold for good features during adaptive 0-255"},
			{"F" , "classify_adapt_proto_threshold","230","Threshold for good protos during adaptive 0-255"},
			{"F" , "classify_adapted_pruning_factor","2.5","Prune poor adapted results this much worse than best result"},
			{"F" , "classify_adapted_pruning_threshold","-1","Threshold at which classify_adapted_pruning_factor starts"},
			{"F" , "classify_bln_numeric_mode","0","Assume the input is numbers [0-9]."},
			{"F" , "classify_char_norm_range","0.2","Character Normalization Range ..."},
			{"F" , "classify_character_fragments_garbage_certainty_threshold","-3","Exclude fragments that do not look like whole characters from training and adaption"},
			{"F" , "classify_class_pruner_multiplier","15","Class Pruner Multiplier 0-255: "},
			{"F" , "classify_class_pruner_threshold","229","Class Pruner Threshold 0-255"},
			{"F" , "classify_cp_angle_pad_loose","45","Class Pruner Angle Pad Loose"},
			{"F" , "classify_cp_angle_pad_medium","20","Class Pruner Angle Pad Medium"},
			{"F" , "classify_cp_angle_pad_tight","10","CLass Pruner Angle Pad Tight"},
			{"F" , "classify_cp_cutoff_strength","7","Class Pruner CutoffStrength:"},
			{"F" , "classify_cp_end_pad_loose","0.5","Class Pruner End Pad Loose"},
			{"F" , "classify_cp_end_pad_medium","0.5","Class Pruner End Pad Medium"},
			{"F" , "classify_cp_end_pad_tight","0.5","Class Pruner End Pad Tight"},
			{"F" , "classify_cp_side_pad_loose","2.5","Class Pruner Side Pad Loose"},
			{"F" , "classify_cp_side_pad_medium","1.2","Class Pruner Side Pad Medium"},
			{"F" , "classify_cp_side_pad_tight","0.6","Class Pruner Side Pad Tight"},
			{"F" , "classify_debug_character_fragments","0","Bring up graphical debugging windows for fragments training"},
			{"F" , "classify_debug_level","0","Classify debug level"},
			{"F" , "classify_enable_adaptive_debugger","0","Enable match debugger"},
			{"F" , "classify_enable_adaptive_matcher","1","Enable adaptive classifier"},
			{"F" , "classify_enable_learning","1","Enable adaptive classifier waiting if >1"},
			{"F" , "classify_font_name","UnknownFontDefault","font name to be used in training"},
			{"F" , "classify_integer_matcher_multiplier","10","Integer Matcher Multiplier0-255: "},
			{"F" , "classify_learn_debug_str","","Class str to debug learning"},
			{"F" , "classify_learning_debug_level","0","Learning Debug Level: "},
			{"F" , "classify_max_certainty_margin","5.5","Veto difference between classifier certainties"},
			{"F" , "classify_max_norm_scale_x","0.325 ","Max char x-norm scale ..."},
			{"F" , "classify_max_norm_scale_y","0.325 ","Max char y-norm scale ..."},
			{"F" , "classify_max_rating_ratio","1.5","Veto ratio between classifier ratings"},
			{"F" , "classify_max_slope","241.421","Slope above which lines are called vertical"},
			{"F" , "classify_min_norm_scale_x","0","Min char x-norm scale ..."},
			{"F" , "classify_min_norm_scale_y","0","Min char y-norm scale ..."},
			{"F" , "classify_min_slope","0.414214","Slope below which lines are called horizontal"},
			{"F" , "classify_misfit_junk_penalty","0","Penalty to apply when a non-alnum is vertically out of its expected textline position"},
			{"F" , "classify_nonlinear_norm","0","Non-linear stroke-density normalization"},
			{"F" , "classify_norm_adj_curl","2","Norm adjust curl ..."},
			{"F" , "classify_norm_adj_midpoint","32","Norm adjust midpoint ..."},
			{"F" , "classify_norm_method","1","Normalization Method ..."},
			{"F" , "classify_num_cp_levels","3","Number of Class Pruner Levels"},
			{"F" , "classify_pico_feature_length","0.05","Pico Feature Length"},
			{"F" , "classify_pp_angle_pad","45","Proto Pruner Angle Pad"},
			{"F" , "classify_pp_end_pad","0.5","Proto Prune End Pad"},
			{"F" , "classify_pp_side_pad","2.5","Proto Pruner Side Pad"},
			{"F" , "classify_save_adapted_templates","0","Save adapted templates to a file"},
			{"F" , "classify_training_file","MicroFeatures","Training file"},
			{"F" , "classify_use_pre_adapted_templates","0","Use pre-adapted classifier templates"},
			{"F" , "conflict_set_I_l_1","Il1[]","Il1"},
			{"F" , "crunch_accept_ok","1","Use acceptability in okstring"},
			{"F" , "crunch_debug","0","As it says"},
			{"F" , "crunch_del_cert","-10","POTENTIAL crunch cert lt this"},
			{"F" , "crunch_del_high_word","1.5","Del if word gt xht x this above bl"},
			{"F" , "crunch_del_low_word","0.5","Del if word gt xht x this below bl"},
			{"F" , "crunch_del_max_ht","3","Del if word ht gt xht x this"},
			{"F" , "crunch_del_min_ht","0.7","Del if word ht lt xht x this"},
			{"F" , "crunch_del_min_width","3","Del if word width lt xht x this"},
			{"F" , "crunch_del_rating","60","POTENTIAL crunch rating lt this"},
			{"F" , "crunch_early_convert_bad_unlv_chs","0","Take out ~^ early?"},
			{"F" , "crunch_early_merge_tess_fails","1","Before word crunch?"},
			{"F" , "crunch_include_numerals","0","Fiddle alpha figures"},
			{"F" , "crunch_leave_accept_strings","0","Don't pot crunch sensible strings"},
			{"F" , "crunch_leave_lc_strings","4","Don't crunch words with long lower case strings"},
			{"F" , "crunch_leave_ok_strings","1","Don't touch sensible strings"},
			{"F" , "crunch_leave_uc_strings","4","Don't crunch words with long lower case strings"},
			{"F" , "crunch_long_repetitions","3","Crunch words with long repetitions"},
			{"F" , "crunch_poor_garbage_cert","-9","crunch garbage cert lt this"},
			{"F" , "crunch_poor_garbage_rate","60","crunch garbage rating lt this"},
			{"F" , "crunch_pot_garbage","1","POTENTIAL crunch garbage"},
			{"F" , "crunch_pot_indicators","1","How many potential indicators needed"},
			{"F" , "crunch_pot_poor_cert","-8","POTENTIAL crunch cert lt this"},
			{"F" , "crunch_pot_poor_rate","40","POTENTIAL crunch rating lt this"},
			{"F" , "crunch_rating_max","10","For adj length in rating per ch"},
			{"F" , "crunch_small_outlines_size","0.6","Small if lt xht x this"},
			{"F" , "crunch_terrible_garbage","1","As it says"},
			{"F" , "crunch_terrible_rating","80","crunch rating lt this"},
			{"F" , "dawg_debug_level","0","Set to 1 for general debug info"},
			{"F" , "debug_acceptable_wds","0","Dump word pass/fail chk"},
			{"T" , "debug_file","$$DEBUGFILE$$","File to send tprintf output to"},
			{"F" , "debug_fix_space_level","0","Contextual fixspace debug"},
			{"F" , "debug_noise_removal","0","Debug reassignment of small outlines"},
			{"F" , "debug_x_ht_level","0","Reestimate debug"},
			{"F" , "devanagari_split_debugimage","0","Whether to create a debug image for split shiro-rekha process."},
			{"F" , "devanagari_split_debuglevel","0","Debug level for split shiro-rekha process."},
			{"F" , "disable_character_fragments","1","Do not include character fragments in the results of the classifier"},
			{"F" , "doc_dict_certainty_threshold","-2.25","Worst certainty for words that can be inserted into thedocument dictionary"},
			{"F" , "doc_dict_pending_threshold","0","Worst certainty for using pending dictionary"},
			{"F" , "docqual_excuse_outline_errs","0","Allow outline errs in unrejection?"},
			{"F" , "edges_boxarea","0.875","Min area fraction of grandchild for box"},
			{"F" , "edges_childarea","0.5","Min area fraction of child outline"},
			{"F" , "edges_children_count_limit","45","Max holes allowed in blob"},
			{"F" , "edges_children_fix","0","Remove boxy parents of char-like children"},
			{"F" , "edges_children_per_grandchild","10","Importance ratio for chucking outlines"},
			{"F" , "edges_debug","0","turn on debugging for this module"},
			{"F" , "edges_max_children_layers","5","Max layers of nested children inside a character outline"},
			{"F" , "edges_max_children_per_outline","10","Max number of children inside a character outline"},
			{"F" , "edges_min_nonhole","12","Min pixels for potential char in box"},
			{"F" , "edges_patharea_ratio","40","Max lensq/area for acceptable child outline"},
			{"F" , "edges_use_new_outline_complexity","0","Use the new outline complexity module"},
			{"F" , "editor_dbwin_height","24","Editor debug window height"},
			{"F" , "editor_dbwin_name","EditorDBWinEditor","debug window name"},
			{"F" , "editor_dbwin_width","80","Editor debug window width"},
			{"F" , "editor_dbwin_xpos","50","Editor debug window X Pos"},
			{"F" , "editor_dbwin_ypos","500","Editor debug window Y Pos"},
			{"F" , "editor_debug_config_file","","Config file to apply to single words"},
			{"F" , "editor_image_blob_bb_color","4","Blob bounding box colour"},
			{"F" , "editor_image_menuheight","50","Add to image height for menu bar"},
			{"F" , "editor_image_text_color","2","Correct text colour"},
			{"F" , "editor_image_win_name","EditorImage","Editor image window name"},
			{"F" , "editor_image_word_bb_color","7","Word bounding box colour"},
			{"F" , "editor_image_xpos","590","Editor image X Pos"},
			{"F" , "editor_image_ypos","10","Editor image Y Pos"},
			{"F" , "editor_word_height","240","Word window height"},
			{"F" , "editor_word_name","BlnWords","BL normalized word window"},
			{"F" , "editor_word_width","655","Word window width"},
			{"F" , "editor_word_xpos","60","Word window X Pos"},
			{"F" , "editor_word_ypos","510","Word window Y Pos"},
			{"F" , "enable_new_segsearch","1","Enable new segmentation search path."},
			{"F" , "enable_noise_removal","1","Remove and conditionally reassign small outlines when they confuse layout analysis"},
			{"F" , "equationdetect_save_bi_image","0","Save input bi image"},
			{"F" , "equationdetect_save_merged_image","0","Save the merged image"},
			{"F" , "equationdetect_save_seed_image","0","Save the seed image"},
			{"F" , "equationdetect_save_spt_image","0","Save special character image"},
			{"F" , "file_type",".tif","Filename extension"},
			{"F" , "fixsp_done_mode","1","What constitues done for spacing"},
			{"F" , "fixsp_non_noise_limit","1","How many non-noise blbs either side?"},
			{"F" , "fixsp_small_outlines_size","","0.28 Small if lt xht x this"},
			{"F" , "force_word_assoc","0","force associator to run regardless of what enable_assoc is.This is used for CJK where component grouping is necessary."},
			{"F" , "fragments_debug","0","Debug character fragments"},
			{"F" , "fragments_guide_chopper","0","Use information from fragments to guide chopping process"},
			{"F" , "fx_debugfile","FXDebug","Name of debugfile"},
			{"F" , "gapmap_big_gaps","1.75","xht multiplier"},
			{"F" , "gapmap_debug","0","Say which blocks have tables"},
			{"F" , "gapmap_no_isolated_quanta","0","Ensure gaps not less than 2quanta wide"},
			{"F" , "gapmap_use_ends","0","Use large space at start and end of rows"},
			{"F" , "heuristic_max_char_wh_ratio","2","max char width-to-height ratio allowed in segmentation"},
			{"F" , "heuristic_segcost_rating_base","1.25","base factor for adding segmentation cost into word rating.It's a multiplying factor"},
			{"F" , "heuristic_weight_rating","1","weight associated with char rating in combined cost ofstate"},
			{"F" , "heuristic_weight_seamcut","0","weight associated with seam cut in combined cost of state"},
			{"F" , "heuristic_weight_width","1000","weight associated with width evidence in combined cost of state"},
			{"F" , "hocr_font_info","0","Add font info to hocr output"},
			{"F" , "hyphen_debug_level","0","Debug level for hyphenated words."},
			{"F" , "il1_adaption_test","0","Don't adapt to i/I at beginning of word"},
			{"F" , "include_page_breaks","0","Include page separator string in output text after each image/page."},
			{"F" , "interactive_display_mode","0","Run interactively?"},
			{"F" , "language_model_debug_level","0","Language model debug level"},
			{"F" , "language_model_fixed_length_choices_depth","3","Depth of blob choice lists to explore when fixed length dawgs are on"},
			{"F" , "language_model_min_compound_length","3","Minimum length of compound words"},
			{"F" , "language_model_ngram_nonmatch_score","-40","Average classifier score of a non-matching unichar."},
			{"F" , "language_model_ngram_on","0","Turn on/off the use of character ngram model"},
			{"F" , "language_model_ngram_order","8","Maximum order of the character ngram model"},
			{"F" , "language_model_ngram_rating_factor","16","Factor to bring log-probs into the same range as ratings when multiplied by outline length "},
			{"F" , "language_model_ngram_scale_factor","0.03","Strength of the character ngram model relative to the character classifier "},
			{"F" , "language_model_ngram_small_prob","1,00E-06","To avoid overly small denominators use this as the floor of the probability returned by the ngram model."},
			{"F" , "language_model_ngram_space_delimited_language","1","Words are delimited by space"},
			{"F" , "language_model_ngram_use_only_first_uft8_step","0","Use only the first UTF8 step of the given string when computing log probabilities."},
			{"F" , "language_model_penalty_case","0.1","Penalty for inconsistent case"},
			{"F" , "language_model_penalty_chartype","0.3","Penalty for inconsistent character type"},
			{"F" , "language_model_penalty_font","0","Penalty for inconsistent font"},
			{"F" , "language_model_penalty_increment","0.01","Penalty increment"},
			{"F" , "language_model_penalty_non_dict_word","0.15","Penalty for non-dictionary words"},
			{"F" , "language_model_penalty_non_freq_dict_word","0.1","Penalty for words not in the frequent word dictionary"},
			{"F" , "language_model_penalty_punc","0.2","Penalty for inconsistent punctuation"},
			{"F" , "language_model_penalty_script","0.5","Penalty for inconsistent script"},
			{"F" , "language_model_penalty_spacing","0.05","Penalty for inconsistent spacing"},
			{"F" , "language_model_use_sigmoidal_certainty","0","Use sigmoidal score for certainty"},
			{"F" , "language_model_viterbi_list_max_num_prunable","10","Maximum number of prunable (those for which PrunablePath() is true) entries in each viterbi list recorded in BLOB_CHOICEs"},
			{"F" , "language_model_viterbi_list_max_size","500","Maximum size of viterbi lists recorded in BLOB_CHOICEs"},
			{"F" , "load_bigram_dawg","1","Load dawg with special word bigrams."},
			{"F" , "load_fixed_length_dawgs","1","Load fixed length dawgs (e.g. for non-space delimited languages)"},
			{"F" , "load_freq_dawg","1","Load frequent word dawg."},
			{"F" , "load_number_dawg","1","Load dawg with number patterns."},
			{"F" , "load_punc_dawg","1","Load dawg with punctuation patterns."},
			{"F" , "load_system_dawg","1","Load system word dawg."},
			{"F" , "load_unambig_dawg","1","Load unambiguous word dawg."},
			{"F" , "lstm_use_matrix","1","Use ratings matrix/beam search with lstm"},
			{"F" , "m_data_sub_dir","tessdata/","Directory for data files"},
			{"F" , "matcher_avg_noise_size","12","Avg. noise blob length"},
			{"F" , "matcher_bad_match_pad","0.15","Bad Match Pad (0-1)"},
			{"F" , "matcher_clustering_max_angle_delta","0.015","Maximum angle delta for prototype clustering"},
			{"F" , "matcher_debug_flags","0","Matcher Debug Flags"},
			{"F" , "matcher_debug_level","0","Matcher Debug Level"},
			{"F" , "matcher_debug_separate_windows","0","Use two different windows for debugging the matching: One for the protos and one for the features."},
			{"F" , "matcher_good_threshold","0.125"," Good Match (0-1)"},
			{"F" , "matcher_min_examples_for_prototyping","3","Reliable Config Threshold"},
			{"F" , "matcher_perfect_threshold","0.02","Perfect Match (0-1)"},
			{"F" , "matcher_permanent_classes_min","1","Min # of permanent classes"},
			{"F" , "matcher_rating_margin","0.1","New template margin (0-1)"},
			{"F" , "matcher_reliable_adaptive_result","0","Great Match (0-1)"},
			{"F" , "matcher_sufficient_examples_for_prototyping","5","Enable adaption even if the ambiguities have not been seen"},
			{"F" , "max_permuter_attempts","10000","Maximum number of different character choices to consider during permutation. This limit is especially useful when user patterns are specified"},
			{"F" , "max_viterbi_list_size","10","Maximum size of viterbi list."},
			{"F" , "merge_fragments_in_matrix","1","Merge the fragments in the ratings matrix and delete them after merging"},
			{"F" , "min_orientation_margin","7","Min acceptable orientation margin"},
			{"F" , "min_sane_x_ht_pixels","8","Reject any x-ht lt or eq than this"},
			{"F" , "multilang_debug_level","0","Print multilang debug info."},
			{"F" , "ngram_permuter_activated","0","Activate character-level n-gram-based permuter"},
			{"F" , "noise_cert_basechar","-8","Hingepoint for base char certainty"},
			{"F" , "noise_cert_disjoint","-1","Hingepoint for disjoint certainty"},
			{"F" , "noise_cert_factor","0.375","Scaling on certainty diff from Hingepoint"},
			{"F" , "noise_cert_punc","-3","Threshold for new punc char certainty"},
			{"F" , "noise_maxperblob","8","Max diacritics to apply to a blob"},
			{"F" , "noise_maxperword","16","Max diacritics to apply to a word"},
			{"F" , "numeric_punctuation","","Punct. chs expected WITHIN numbers"},
			{"F" , "ocr_devanagari_split_strategy","0","Whether to use the top-line splitting process for Devanagari documents while performing ocr."},
			{"F" , "ok_repeated_ch_non_alphanum_wds","-?*=&","Allow NN to unrej"},
			{"F" , "oldbl_corrfix","1","Improve correlation of heights"},
			{"F" , "oldbl_dot_error_size","1.26","Max aspect ratio of a dot"},
			{"F" , "oldbl_holed_losscount","10","Max lost before fallback line used"},
			{"F" , "oldbl_xhfix","0","Fix bug in modes threshold for xheights"},
			{"F" , "oldbl_xhfract","0.4","Fraction of est allowed in calc"},
			{"F" , "outlines_2","ij!?%:;","Non standard number of outlines"},
			{"F" , "outlines_odd%","","Non standard number of outlines"},
			{"F" , "output_ambig_words_file","","Output file for ambiguities found in the dictionary"},
			{"F" , "page_separator","","Page separator (default is form feed control character) word delimiter spaces are assumed to have variable width even though characters have fixed pitch"},
			{"F" , "pageseg_devanagari_split_strategy","0","Whether to use the top-line splitting process for Devanagari documents while performing page-segmentation."},
			{"T" , "paragraph_debug_level","1","Print paragraph debug info."},
			{"F" , "paragraph_text_based","1","Run paragraph detection on the post-text-recognition (more accurate)"},
			{"F" , "permute_chartype_word","0","Turn on character type (property) consistency permuter"},
			{"F" , "permute_debug","0","Debug char permutation process"},
			{"F" , "permute_fixed_length_dawg","0","Turn on fixed-length phrasebook search permuter"},
			{"F" , "permute_only_top","0","Run only the top choice permuter"},
			{"F" , "permute_script_word","0","Turn on word script consistency permuter"},
			{"F" , "pitsync_fake_depth","1","Max advance fake generation"},
			{"F" , "pitsync_joined_edge","0.75","Dist inside big blob for chopping"},
			{"F" , "pitsync_linear_version","6","Use new fast algorithm"},
			{"F" , "pitsync_offset_freecut_fraction","0.25","Fraction of cut for free cuts"},
			{"F" , "poly_allow_detailed_fx","0","Allow feature extractors to see the original outline"},
			{"F" , "poly_debug","0","Debug old poly"},
			{"F" , "poly_wide_objects_better","1","More accurate approx on wide things"},
			{"F" , "preserve_interword_spaces","0","Preserve multiple interword spaces"},
			{"F" , "prioritize_division","0","Prioritize blob division over chopping"},
			{"F" , "quality_blob_pc","0","good_quality_doc gte good blobs limit"},
			{"F" , "quality_char_pc","0.95","good_quality_doc gte good char limit"},
			{"F" , "quality_min_initial_alphas_reqd","2","alphas in a good word"},
			{"F" , "quality_outline_pc","1","good_quality_doc lte outline error limit"},
			{"F" , "quality_rej_pc","0.08","good_quality_doc lte rejection limit"},
			{"F" , "quality_rowrej_pc","1.1","good_quality_doc gte good char limit"},
			{"F" , "rating_scale","1.5","Rating scaling factor"},
			{"F" , "rej_1Il_trust_permuter_type","1","Don't double check"},
			{"F" , "rej_1Il_use_dict_word","0","Use dictword test"},
			{"F" , "rej_alphas_in_number_perm","0","Extend permuter check"},
			{"F" , "rej_trust_doc_dawg","0","Use DOC dawg in 11l conf. detector"},
			{"F" , "rej_use_good_perm","1","Individual rejection control"},
			{"F" , "rej_use_sensible_wd","0","Extend permuter check"},
			{"F" , "rej_use_tess_accepted","1","Individual rejection control"},
			{"F" , "rej_use_tess_blanks","1","Individual rejection control"},
			{"F" , "rej_whole_of_mostly_reject_word_fract","0.85"," if >this fract"},
			{"F" , "repair_unchopped_blobs","1","Fix blobs that aren't chopped"},
			{"F" , "save_alt_choices","1","Save alternative paths found during chopping and segmentation search"},
			{"F" , "save_doc_words","0","Save Document Words"},
			{"F" , "save_raw_choices","0","Deprecated- backward compatibility only"},
			{"F" , "segment_adjust_debug","0","Segmentation adjustment debug"},
			{"F" , "segment_debug","0","Debug the whole segmentation process"},
			{"F" , "segment_nonalphabetic_script","0","Don't use any alphabetic-specific tricks.Set to true in the traineddata config file for scripts that are cursive or inherently fixed-pitch"},
			{"F" , "segment_penalty_dict_case_bad","13.125","Default score multiplier for word matches which may have case issues (lower is better)."},
			{"F" , "segment_penalty_dict_case_ok","1.1","Score multiplier for word matches that have good case (lower is better)."},
			{"F" , "segment_penalty_dict_frequent_word","1","Score multiplier for word matches which have good case andare frequent in the given language (lower is better)."},
			{"F" , "segment_penalty_dict_nonword","1.25","Score multiplier for glyph fragment segmentations which do not match a dictionary word (lower is better)."},
			{"F" , "segment_penalty_garbage","1.5","Score multiplier for poorly cased strings that are not in the dictionary and generally look like garbage (lower is better)."},
			{"F" , "segment_penalty_ngram_best_choice","1.24","Multipler to for the best choice from the ngram model."},
			{"F" , "segment_reward_chartype","0.97","Score multipler for char type consistency within a word. "},
			{"F" , "segment_reward_ngram_best_choice","0.99","Score multipler for ngram permuter's best choice (only used in the Han script path)."},
			{"F" , "segment_reward_script","0.95","Score multipler for script consistency within a word. Being a 'reward' factor"},
			{"F" , "segment_segcost_rating","0","incorporate segmentation cost in word rating?"},
			{"F" , "segsearch_debug_level","0","SegSearch debug level"},
			{"F" , "segsearch_max_char_wh_ratio","2","Maximum character width-to-height ratio"},
			{"F" , "segsearch_max_fixed_pitch_char_wh_ratio","2","Maximum character width-to-height ratio for fixed-pitch fonts"},
			{"F" , "segsearch_max_futile_classifications","20","Maximum number of pain point classifications per chunk thatdid not result in finding a better word choice."},
			{"F" , "segsearch_max_pain_points" , "2000" , "Maximum number of pain points stored in the queue"},
			{"F" , "speckle_large_max_size","0.3","Max large speckle size"},
			{"F" , "speckle_rating_penalty","10","Penalty to add to worst rating for noise"},
			{"F" , "stopper_allowable_character_badness","3","Max certaintly variation allowed in a word (in sigma)"},
			{"F" , "stopper_certainty_per_char","-0.5","Certainty to add for each dict char above small word size."},
			{"F" , "stopper_debug_level","0","Stopper debug level"},
			{"F" , "stopper_no_acceptable_choices","0","Make AcceptableChoice() always return false. Useful when there is a need to explore all segmentations"},
			{"F" , "stopper_nondict_certainty_base","-2.5","Certainty threshold for non-dict words"},
			{"F" , "stopper_phase2_certainty_rejection_offset","1","Reject certainty offset"},
			{"F" , "stopper_smallword_size","2","Size of dict word to be treated as non-dict word"},
			{"F" , "stream_filelist","0","Stream a filelist from stdin"},
			{"F" , "subscript_max_y_top","0.5","Maximum top of a character measured as a multiple of x-height above the baseline for us to reconsider whether it's a subscript."},
			{"F" , "superscript_bettered_certainty","0.97 ","What reduction in badness do we think sufficient to choose a superscript over what we'd thought.For example a value of 0.6 means we want to reduce badness of certainty by at least 40%"},
			{"F" , "superscript_debug","0","Debug level for sub & superscript fixer"},
			{"F" , "superscript_min_y_bottom","0.3","Minimum bottom of a character measured as a multiple of x-height above the baseline for us to reconsider whether it's a superscript."},
			{"F" , "superscript_scaledown_ratio","0.4","A superscript scaled down more than this is unbelievably small.For example"},
			{"F" , "superscript_worse_certainty","2","How many times worse certainty does a superscript position glyph need to be for us to try classifying it as a char with a different baseline?"},
			{"F" , "suspect_accept_rating","-999.9","Accept good rating limit"},
			{"F" , "suspect_constrain_1Il","0","UNLV keep 1Il chars rejected"},
			{"F" , "suspect_level","99","Suspect marker level"},
			{"F" , "suspect_rating_per_ch","999.9","Don't touch bad rating limit"},
			{"F" , "suspect_short_words","2","Don't suspect dict wds longer than this"},
			{"F" , "suspect_space_level","100","Min suspect level for rejecting spaces"},
			{"F" , "tess_bn_matching","0","Baseline Normalized Matching"},
			{"F" , "tess_cn_matching","0","Character Normalized Matching"},
			{"F" , "tessdata_manager_debug_level","0","Debug level for TessdataManager functions."},
			{"F" , "tessedit_adaption_debug","0","Generate and print debug information for adaption"},
			{"F" , "tessedit_ambigs_training","0","Perform training for ambiguities"},
			{"F" , "tessedit_bigram_debug","0","Amount of debug output for bigram correction."},
			{"F" , "tessedit_certainty_threshold","-2.25","Good blob limit"},
			{"F" , "tessedit_char_blacklist","","Blacklist of chars not to recognize"},
			{"F" , "tessedit_char_unblacklist","","List of chars to override tessedit_char_blacklist"},
			{"T" , "tessedit_char_whitelist","ABCDEFGHIJKLMNOPQRSDTUVWXYZ012345789","Whitelist of chars to recognize"},
			{"F" , "tessedit_class_miss_scale","0.00390625","Scale factor for features not used"},
			{"F" , "tessedit_consistent_reps","1","Force all rep chars the same"},
			{"F" , "tessedit_create_boxfile","0","Output text with boxes"},
			{"F" , "tessedit_create_hocr","0","Write .html hOCR output file"},
			{"F" , "tessedit_create_pdf","0","Write .pdf output file"},
			{"F" , "tessedit_create_tsv","0","Write .tsv output file"},
			{"F" , "tessedit_create_txt","0","Write .txt output file"},
			{"F" , "tessedit_debug_block_rejection","0","Block and Row stats"},
			{"F" , "tessedit_debug_doc_rejection","0","Page stats"},
			{"F" , "tessedit_debug_fonts","0","Output font info per char"},
			{"F" , "tessedit_debug_quality_metrics","0","Output data to debug file"},
			{"F" , "tessedit_display_outwords","0","Draw output words"},
			{"F" , "tessedit_dont_blkrej_good_wds","0","Use word segmentation quality metric"},
			{"F" , "tessedit_dont_rowrej_good_wds","0","Use word segmentation quality metric"},
			{"F" , "tessedit_dump_choices","0","Dump char choices"},
			{"F" , "tessedit_dump_pageseg_images","0","Dump intermediate images made during page segmentation"},
			{"F" , "tessedit_enable_bigram_correction","1","Enable correction based on the word bigram dictionary."},
			{"F" , "tessedit_enable_dict_correction","0","Enable single word correction based on the dictionary."},
			{"F" , "tessedit_enable_doc_dict","1","Add words to the document dictionary"},
			{"F" , "tessedit_fix_fuzzy_spaces","1","Try to improve fuzzy spaces"},
			{"F" , "tessedit_fix_hyphens","1","Crunch double hyphens?"},
			{"F" , "tessedit_flip_0O","1","Contextual 0O O0 flips"},
			{"F" , "tessedit_good_doc_still_rowrej_wd","1.1","rej good doc wd if more than this fraction rejected"},
			{"F" , "tessedit_good_quality_unrej","1","Reduce rejection on good docs"},
			{"F" , "tessedit_image_border","2","Rej blbs near image edge limit"},
			{"F" , "tessedit_init_config_only","0","Only initialize with the config file. Useful if the instance is not going to be used for OCR but say only for layout analysis."},
			{"F" , "tessedit_load_sublangs","","List of languages to load with this one"},
			{"F" , "tessedit_lower_flip_hyphen","1.5","Aspect ratio dot/hyphen test"},
			{"F" , "tessedit_make_boxes_from_boxes","0","Generate more boxes from boxed chars"},
			{"F" , "tessedit_matcher_log","0","Log matcher activity"},
			{"F" , "tessedit_minimal_rej_pass1","0","Do minimal rejection on pass 1 output"},
			{"F" , "tessedit_minimal_rejection","0","Only reject tess failures"},
			{"F" , "tessedit_module_name","libtesseract","Module colocated with tessdata dir"},
			{"F" , "tessedit_ocr_engine_mode","2","Which OCR engine(s) to run (Tesseract"},
			{"F" , "tessedit_ok_mode","5","Acceptance decision algorithm"},
			{"F" , "tessedit_override_permuter","1","According to dict_word"},
			{"F" , "tessedit_page_number","-1","-1 -> All pages else specific page to process"},
			{"F" , "tessedit_pageseg_mode","6","Page seg mode: 0=osd only"},
			{"F" , "tessedit_parallelize","0","Run in parallel where possible"},
			{"F" , "tessedit_prefer_joined_punct","0","Reward punctation joins"},
			{"F" , "tessedit_preserve_blk_rej_perfect_wds","1","Only rej partially rejected words in block rejection"},
			{"F" , "tessedit_preserve_min_wd_len","2","Only preserve wds longer than this"},
			{"F" , "tessedit_preserve_row_rej_perfect_wds","1","Only rej partially rejected words in row rejection"},
			{"F" , "tessedit_redo_xheight","1","Check/Correct x-height"},
			{"F" , "tessedit_reject_bad_qual_wds","1","Reject all bad quality wds"},
			{"F" , "tessedit_reject_block_percent","45",""},
			{"F" , "tessedit_reject_doc_percent","65","%rej allowed before rej whole doc"},
			{"F" , "tessedit_reject_mode","0","Rejection algorithm"},
			{"F" , "tessedit_reject_row_percent","40","%rej allowed before rej whole row"},
			{"F" , "tessedit_rejection_debug","0","Adaption debug"},
			{"F" , "tessedit_resegment_from_boxes","0","Take segmentation and labeling from box file"},
			{"F" , "tessedit_resegment_from_line_boxes","0","Conversion of word/line box file to char box file"},
			{"F" , "tessedit_row_rej_good_docs","1","Apply row rejection to good docs"},
			{"F" , "tessedit_single_match","0","Top choice only from CP"},
			{"F" , "tessedit_tess_adaption_mode","39","Adaptation decision algorithm for tess"},
			{"F" , "tessedit_test_adaption","0","Test adaption criteria"},
			{"F" , "tessedit_test_adaption_mode","3","Adaptation decision algorithm for tess"},
			{"F" , "tessedit_timing_debug","0","Print timing stats"},
			{"F" , "tessedit_train_from_boxes","0","Generate training data from boxed chars"},
			{"F" , "tessedit_train_line_recognizer","0","Break input into lines and remap boxes if present"},
			{"F" , "tessedit_truncate_wordchoice_log","10","Max words to keep in list"},
			{"F" , "tessedit_unrej_any_wd","0","Don't bother with word plausibility"},
			{"F" , "tessedit_upper_flip_hyphen","1.8","Aspect ratio dot/hyphen test"},
			{"F" , "tessedit_use_primary_params_model","0","In multilingual mode use params model of the primary language"},
			{"F" , "tessedit_use_reject_spaces","1","Reject spaces?"},
			{"F" , "tessedit_whole_wd_rej_row_percent","70","Number of row rejects in whole word rejectswhich prevents whole row rejection"},
			{"F" , "tessedit_word_for_word","0","Make output have exactly one word per WERD"},
			{"F" , "tessedit_write_block_separators","0","Write block separators in output"},
			{"F" , "tessedit_write_images","0","Capture the image from the IPE"},
			{"F" , "tessedit_write_params_to_file","","Write all parameters to the given file."},
			{"F" , "tessedit_write_rep_codes","0","Write repetition char code"},
			{"F" , "tessedit_write_unlv","0","Write .unlv output file"},
			{"F" , "tessedit_zero_kelvin_rejection","0","Don't reject ANYTHING AT ALL"},
			{"F" , "tessedit_zero_rejection","0","Don't reject ANYTHING"},
			{"F" , "test_pt","0","Test for point"},
			{"F" , "test_pt_x","100000","xcoord"},
			{"F" , "test_pt_y","100000","ycoord"},
			{"F" , "textonly_pdf","0","Create PDF with only one invisible text layer"},
			{"F" , "textord_all_prop","0","All doc is proportial text"},
			{"F" , "textord_ascheight_mode_fraction","0.08","Min pile height to make ascheight"},
			{"F" , "textord_ascx_ratio_max","1.8","Max cap/xheight"},
			{"F" , "textord_ascx_ratio_min","1.25","Min cap/xheight"},
			{"F" , "textord_balance_factor","1","Ding rate for unbalanced char cells"},
			{"F" , "textord_baseline_debug","0","Baseline debug level"},
			{"F" , "textord_biased_skewcalc","1","Bias skew estimates with line length"},
			{"F" , "textord_blob_size_bigile","95","Percentile for large blobs"},
			{"F" , "textord_blob_size_smallile","20","Percentile for small blobs"},
			{"F" , "textord_blockndoc_fixed","0","Attempt whole doc/block fixed pitch"},
			{"F" , "textord_blocksall_fixed","0","Moan about prop blocks"},
			{"F" , "textord_blocksall_prop","0","Moan about fixed pitch blocks"},
			{"F" , "textord_blocksall_testing","0","Dump stats when moaning"},
			{"F" , "textord_blshift_maxshift","0","Max baseline shift"},
			{"F" , "textord_blshift_xfraction","9.99","Min size of baseline shift"},
			{"F" , "textord_chop_width","1.5","Max width before chopping"},
			{"F" , "textord_chopper_test","0","Chopper is being tested."},
			{"F" , "textord_debug_baselines","0","Debug baseline generation"},
			{"F" , "textord_debug_blob","0","Print test blob information"},
			{"F" , "textord_debug_block","0","Block to do debug on"},
			{"F" , "textord_debug_bugs","0","Turn on output related to bugs in tab finding"},
			{"F" , "textord_debug_pitch_metric","0","Write full metric stuff"},
			{"F" , "textord_debug_pitch_test","0","Debug on fixed pitch test"},
			{"F" , "textord_debug_printable","0","Make debug windows printable"},
			{"F" , "textord_debug_tabfind","0","Debug tab finding"},
			{"F" , "textord_debug_xheights","0","Test xheight algorithms"},
			{"F" , "textord_descheight_mode_fraction","0.08","Min pile height to make descheight"},
			{"F" , "textord_descx_ratio_max","0.6","Max desc/xheight"},
			{"F" , "textord_descx_ratio_min","0.25","Min desc/xheight"},
			{"F" , "textord_disable_pitch_test","0","Turn off dp fixed pitch algorithm"},
			{"F" , "textord_dotmatrix_gap","3","Max pixel gap for broken pixed pitch"},
			{"F" , "textord_equation_detect","0","Turn on equation detector"},
			{"F" , "textord_excess_blobsize","1.3","New row made if blob makes row this big"},
			{"F" , "textord_expansion_factor","1","Factor to expand rows by in expand_rows"},
			{"F" , "textord_fast_pitch_test","0","Do even faster pitch algorithm"},
			{"F" , "textord_fix_makerow_bug","1","Prevent multiple baselines"},
			{"F" , "textord_fix_xheight_bug","1","Use spline baseline"},
			{"F" , "textord_force_make_prop_words","0","Force proportional word segmentation on all rows"},
			{"F" , "textord_fp_chop_error","2","Max allowed bending of chop cells"},
			{"F" , "textord_fp_chop_snap","0.5","Max distance of chop pt from vertex"},
			{"F" , "textord_fp_chopping","1","Do fixed pitch chopping"},
			{"F" , "textord_fp_min_width","0.5","Min width of decent blobs"},
			{"F" , "textord_fpiqr_ratio","1.5","Pitch IQR/Gap IQR threshold"},
			{"T" , "textord_heavy_nr","1","Vigorously remove noise"},
			{"F" , "textord_initialasc_ile","0.9","Ile of sizes for xheight guess"},
			{"F" , "textord_initialx_ile","","0.75"},
			{"F" , "textord_interpolating_skew","1","Interpolate across gaps"},
			{"F" , "textord_linespace_iqrlimit","0.2","Max iqr/median for linespace"},
			{"F" , "textord_lms_line_trials","12","Number of linew fits to do"},
			{"F" , "textord_max_blob_overlaps","4","Max number of blobs a big blob can overlap"},
			{"F" , "textord_max_noise_size","7","Pixel size of noise"},
			{"F" , "textord_max_pitch_iqr","0.2","Xh fraction noise in pitch"},
			{"F" , "textord_min_blob_height_fraction","0.75","Min blob height/top to include blob top into xheight stats"},
			{"F" , "textord_min_blobs_in_row","4","Min blobs before gradient counted"},
			{"F" , "textord_min_linesize","1.25","* blob height for initial linesize"},
			{"F" , "textord_min_xheight","10","Min credible pixel xheight"},
			{"F" , "textord_minxh","0.25","fraction of linesize for min xheight"},
			{"F" , "textord_new_initial_xheight","1","Use test xheight mechanism"},
			{"F" , "textord_no_rejects","0","Don't remove noise blobs"},
			{"F" , "textord_noise_area_ratio","0.7","Fraction of bounding box for noise"},
			{"F" , "textord_noise_debug","0","Debug row garbage detector to 2 for more details to 3 to see all the dbug messages"},
			{"F" , "textord_noise_hfract","0.015625","Height fraction to discard outlines as speckle noise"},
			{"F" , "textord_noise_normratio","2","Dot to norm ratio for deletion"},
			{"F" , "textord_noise_rejrows","1","Reject noise-like rows"},
			{"F" , "textord_noise_rejwords","1","Reject noise-like words"},
			{"F" , "textord_noise_rowratio","6","Dot to norm ratio for deletion"},
			{"F" , "textord_noise_sizefraction","10","Fraction of size for maxima"},
			{"F" , "textord_noise_sizelimit","0.5","Fraction of x for big t count"},
			{"F" , "textord_noise_sncount","1","super norm blobs to save row"},
			{"F" , "textord_noise_sxfract","0.4","xh fract width error for norm blobs"},
			{"F" , "textord_noise_syfract","0.2","xh fract height error for norm blobs"},
			{"F" , "textord_noise_translimit","16","Transitions for normal blob"},
			{"F" , "textord_occupancy_threshold"," 0.4","Fraction of neighbourhood"},
			{"F" , "textord_ocropus_mode","0","Make baselines for ocropus"},
			{"F" , "textord_old_baselines","1","Use old baseline algorithm since overly generic patterns can result in dawg search exploring an overly large number of options."},
			{"F" , "textord_old_xheight","0","Use old xheight algorithm"},
			{"F" , "textord_oldbl_debug","0","Debug old baseline generation"},
			{"F" , "textord_oldbl_jumplimit","0.15","X fraction for new partition"},
			{"F" , "textord_oldbl_merge_parts","1","Merge suspect partitions"},
			{"F" , "textord_oldbl_paradef","1","Use para default mechanism"},
			{"F" , "textord_oldbl_split_splines","1","Split stepped splines"},
			{"F" , "textord_overlap_x","0.375","Fraction of linespace for good overlap"},
			{"F" , "textord_parallel_baselines","1","Force parallel baselines"},
			{"F" , "textord_pitch_cheat","0","Use correct answer for fixed/prop"},
			{"F" , "textord_pitch_range","2","Max range test on pitch"},
			{"F" , "textord_pitch_rowsimilarity","0.08","Fraction of xheight for sameness"},
			{"F" , "textord_pitch_scalebigwords","0","Scale scores on big words"},
			{"F" , "textord_projection_scale","0.2","Ding rate for mid-cuts"},
			{"F" , "textord_really_old_xheight","0","Use original wiseowl xheight"},
			{"F" , "textord_restore_underlines","1","Chop underlines & put back"},
			{"F" , "textord_show_blobs","0","Display unsorted blobs"},
			{"F" , "textord_show_boxes","0","Display unsorted blobs"},
			{"F" , "textord_show_expanded_rows","0","Display rows after expanding"},
			{"F" , "textord_show_final_blobs","0","Display blob bounds after pre-ass"},
			{"F" , "textord_show_final_rows","0","Display rows after final fitting"},
			{"F" , "textord_show_fixed_cuts","0","Draw fixed pitch cell boundaries"},
			{"F" , "textord_show_fixed_words","0","Display forced fixed pitch words"},
			{"F" , "textord_show_initial_rows","0","Display row accumulation"},
			{"F" , "textord_show_initial_words","0","Display separate words"},
			{"F" , "textord_show_new_words","0","Display separate words"},
			{"F" , "textord_show_page_cuts","0","Draw page-level cuts"},
			{"F" , "textord_show_parallel_rows","0","Display page correlated rows"},
			{"F" , "textord_show_row_cuts","0","Draw row-level cuts"},
			{"F" , "textord_show_tables","0","Show table regions"},
			{"F" , "textord_single_height_mode","0","Script has no xheigth so use a single mode"},
			{"F" , "textord_skew_ile","0.5","Ile of gradients for page skew"},
			{"F" , "textord_skew_lag","0.02","Lag for skew on row accumulation"},
			{"F" , "textord_skewsmooth_offset","4","For smooth factor"},
			{"F" , "textord_skewsmooth_offset2","1","For smooth factor"},
			{"F" , "textord_space_size_is_variable","0","If true"},
			{"F" , "textord_spacesize_ratiofp","2.8","Min ratio space/nonspace"},
			{"F" , "textord_spacesize_ratioprop","2","Min ratio space/nonspace"},
			{"F" , "textord_spline_medianwin","6","Size of window for spline segmentation"},
			{"F" , "textord_spline_minblobs","8","Min blobs in each spline segment"},
			{"F" , "textord_spline_outlier_fraction","0.1","Fraction of line spacing for outlier"},
			{"F" , "textord_spline_shift_fraction","0.02","Fraction of line spacing for quad"},
			{"F" , "textord_straight_baselines","0","Force straight baselines"},
			{"F" , "textord_tabfind_aligned_gap_fraction","0.75 ","Fraction of height used as a minimum gap for aligned blobs."},
			{"F" , "textord_tabfind_find_tables","1","run table detection"},
			{"F" , "textord_tabfind_force_vertical_text","0","Force using vertical text page mode"},
			{"F" , "textord_tabfind_only_strokewidths","0","Only run stroke widths1=auto+osd 2=auto 3=col 4=block 5=line 6=word 7=char (Values from PageSegMode enum in publictypes.h)"},
			{"F" , "textord_tabfind_show_blocks","0","Show final block bounds"},
			{"F" , "textord_tabfind_show_color_fit","0","Show stroke widths LSTM both) defaults to loading and running het most accurate available"},
			{"F" , "textord_tabfind_show_columns","0","Show column bounds"},
			{"F" , "textord_tabfind_show_finaltabs","0","Show tab vectors"},
			{"F" , "textord_tabfind_show_images","0","Show image blobs"},
			{"F" , "textord_tabfind_show_initial_partitions","0","Show partition bounds"},
			{"F" , "textord_tabfind_show_initialtabs","0","Show tab candidates"},
			{"F" , "textord_tabfind_show_partitions","0","Show partition bounds"},
			{"F" , "textord_tabfind_show_reject_blobs","0","Show blobs rejected as noise"},
			{"F" , "textord_tabfind_show_strokewidths","0","Show stroke widths"},
			{"F" , "textord_tabfind_show_vlines","0","Debug line finding"},
			{"F" , "textord_tabfind_vertical_horizontal_mix","1","find horizontal lines such as headers in vertical page mode"},
			{"F" , "textord_tabfind_vertical_text","1","Enable vertical detection"},
			{"F" , "textord_tabfind_vertical_text_ratio","0.5","Fraction of textlines deemed vertical to use vertical page mode"},
			{"F" , "textord_tablefind_recognize_tables","0","Enables the table recognizer for table layout and filtering."},
			{"F" , "textord_tablefind_show_mark","0","Debug table marking steps in detail"},
			{"F" , "textord_tablefind_show_stats","0","Show page stats used in table finding"},
			{"F" , "textord_tabvector_vertical_box_ratio","0.5","Fraction of box matches required to declare a line vertical"},
			{"F" , "textord_tabvector_vertical_gap_fraction","0.5","max fraction of mean blob width allowed for vertical gaps in vertical text"},
			{"F" , "textord_test_landscape","0","Tests refer to land/port"},
			{"F" , "textord_test_mode","0","Do current test"},
			{"F" , "textord_test_x","-2147483647","coord of test pt"},
			{"F" , "textord_test_y","-2147483647","coord of test pt"},
			{"F" , "textord_testregion_bottom","2147483647","Bottom edge of debug rectangle"},
			{"F" , "textord_testregion_left","-1","Left edge of debug reporting rectangle"},
			{"F" , "textord_testregion_right","2147483647","Right edge of debug rectangle"},
			{"F" , "textord_testregion_top","-1","Top edge of debug reporting rectangle"},
			{"F" , "textord_underline_offset","0.1","Fraction of x to ignore"},
			{"F" , "textord_underline_threshold ","0.5","Fraction of width occupied"},
			{"F" , "textord_underline_width","2","Multiple of line_size for underline"},
			{"F" , "textord_use_cjk_fp_model","0","Use CJK fixed pitch model"},
			{"F" , "textord_width_limit","8","Max width of blobs to make rows"},
			{"F" , "textord_width_smooth_factor","0.1","Smoothing width stats"},
			{"F" , "textord_words_def_fixed","0.016","Threshold for definite fixed"},
			{"F" , "textord_words_def_prop","0.09","Threshold for definite prop"},
			{"F" , "textord_words_default_maxspace","3.5","Max believable third space"},
			{"F" , "textord_words_default_minspace","0.6","Fraction of xheight"},
			{"F" , "textord_words_default_nonspace","0.2","Fraction of xheight"},
			{"F" , "textord_words_definite_spread","0.3","Non-fuzzy spacing region"},
			{"F" , "textord_words_initial_lower","0.25","Max initial cluster size"},
			{"F" , "textord_words_initial_upper","0.15","Min initial cluster spacing"},
			{"F" , "textord_words_maxspace","4","Multiple of xheight"},
			{"F" , "textord_words_min_minspace","0.3","Fraction of xheight"},
			{"F" , "textord_words_minlarge","0.75","Fraction of valid gaps needed"},
			{"F" , "textord_words_pitchsd_threshold","0.04 ","Pitch sync threshold"},
			{"F" , "textord_words_veto_power","5","Rows required to outvote a veto"},
			{"F" , "textord_words_width_ile","0.4","Ile of blob widths for space est"},
			{"F" , "textord_wordstats_smooth_factor","0.05","Smoothing gap stats"},
			{"F" , "textord_xheight_error_margin","0.1","Accepted variation"},
			{"F" , "textord_xheight_mode_fraction","0.4","Min pile height to make xheight"},
			{"F" , "tosp_all_flips_fuzzy","0","Pass ANY flip to context?"},
			{"F" , "tosp_block_use_cert_spaces","1","Only stat OBVIOUS spaces"},
			{"F" , "tosp_debug_level","0","Debug data"},
			{"F" , "tosp_dont_fool_with_small_kerns","-1","Limit use of xht gap with odd small kns"},
			{"F" , "tosp_enough_small_gaps","0.65 ","Fract of kerns reqd for isolated row stats"},
			{"F" , "tosp_enough_space_samples_for_median","3","or should we use mean"},
			{"F" , "tosp_few_samples","40","No.gaps reqd with 1 large gap to treat as a table"},
			{"F" , "tosp_flip_caution","0","Don't autoflip kn to sp when large separation"},
			{"F" , "tosp_flip_fuzz_kn_to_sp","1","Default flip"},
			{"F" , "tosp_flip_fuzz_sp_to_kn","1","Default flip"},
			{"F" , "tosp_force_wordbreak_on_punct","0","Force word breaks on punct to break long lines in non-space delimited langs"},
			{"F" , "tosp_fuzzy_kn_fraction","0.5","New fuzzy kn alg"},
			{"F" , "tosp_fuzzy_limit_all","1","Don't restrict kn->sp fuzzy limit to tables"},
			{"F" , "tosp_fuzzy_sp_fraction","0.5","New fuzzy sp alg"},
			{"F" , "tosp_fuzzy_space_factor","0.6","Fract of xheight for fuzz sp"},
			{"F" , "tosp_fuzzy_space_factor1","0.5","Fract of xheight for fuzz sp"},
			{"F" , "tosp_fuzzy_space_factor2","0.72","Fract of xheight for fuzz sp"},
			{"F" , "tosp_gap_factor","0.83","gap ratio to flip sp->kern"},
			{"F" , "tosp_ignore_big_gaps","-1","xht multiplier"},
			{"F" , "tosp_ignore_very_big_gaps","3.5","xht multiplier"},
			{"F" , "tosp_improve_thresh","0","Enable improvement heuristic"},
			{"F" , "tosp_init_guess_kn_mult","2.2","Thresh guess - mult kn by this"},
			{"F" , "tosp_init_guess_xht_mult","0.28 ","Thresh guess - mult xht by this"},
			{"F" , "tosp_kern_gap_factor1","2","gap ratio to flip kern->sp"},
			{"F" , "tosp_kern_gap_factor2","1.3","gap ratio to flip kern->sp"},
			{"F" , "tosp_kern_gap_factor3","2.5","gap ratio to flip kern->sp"},
			{"F" , "tosp_large_kerning","0.19","Limit use of xht gap with large kns"},
			{"F" , "tosp_max_sane_kn_thresh","5","Multiplier on kn to limit thresh"},
			{"F" , "tosp_min_sane_kn_sp","1.5","Don't trust spaces less than this time kn"},
			{"F" , "tosp_narrow_aspect_ratio","0.48 ","narrow if w/h less than this"},
			{"F" , "tosp_narrow_blobs_not_cert","1","Only stat OBVIOUS spaces"},
			{"F" , "tosp_narrow_fraction","0.3","Fract of xheight for narrow"},
			{"F" , "tosp_near_lh_edge","0","Don't reduce box if the top left is non blank"},
			{"F" , "tosp_old_sp_kn_th_factor","2","Factor for defining space threshold in terms of space and kern sizes"},
			{"F" , "tosp_old_to_bug_fix0","","Fix suspected bug in old code"},
			{"F" , "tosp_old_to_constrain_sp_kn","0","Constrain relative values of inter and intra-word gaps for old_to_method."},
			{"F" , "tosp_old_to_method","0","Space stats use prechopping?"},
			{"F" , "tosp_only_small_gaps_for_kern","0","Better guess"},
			{"F" , "tosp_only_use_prop_rows","1","Block stats to use fixed pitch rows?"},
			{"F" , "tosp_only_use_xht_gaps","0","Only use within xht gap for wd breaks"},
			{"F" , "tosp_pass_wide_fuzz_sp_to_context ","0.75"," How wide fuzzies need context"},
			{"F" , "tosp_recovery_isolated_row_stats","1","Use row alone when inadequate cert spaces"},
			{"F" , "tosp_redo_kern_limit","10","No.samples reqd to reestimate for row"},
			{"F" , "tosp_rep_space","1.6","rep gap multiplier for space"},
			{"F" , "tosp_row_use_cert_spaces","1","Only stat OBVIOUS spaces"},
			{"F" , "tosp_row_use_cert_spaces","11","Only stat OBVIOUS spaces"},
			{"F" , "tosp_rule_9_test_punct","0","Don't chng kn to space next to punct"},
			{"F" , "tosp_sanity_method","1","How to avoid being silly"},
			{"F" , "tosp_short_row","20","No.gaps reqd with few cert spaces to use certs"},
			{"F" , "tosp_silly_kn_sp_gap","0.2","Don't let sp minus kn get too small"},
			{"F" , "tosp_stats_use_xht_gaps","1","Use within xht gap for wd breaks"},
			{"F" , "tosp_table_fuzzy_kn_sp_ratio","3","Fuzzy if less than this"},
			{"F" , "tosp_table_kn_sp_ratio","2.25 ","Min difference of kn & sp in table"},
			{"F" , "tosp_table_xht_sp_ratio","0.33 ","Expect spaces bigger than this"},
			{"F" , "tosp_threshold_bias1","0","how far between kern and space?"},
			{"F" , "tosp_threshold_bias2","0","how far between kern and space?"},
			{"F" , "tosp_use_pre_chopping","0","Space stats use prechopping?"},
			{"F" , "tosp_use_xht_gaps","1","Use within xht gap for wd breaks"},
			{"F" , "tosp_wide_aspect_ratio","0","wide if w/h less than this"},
			{"F" , "tosp_wide_fraction","0.52","Fract of xheight for wide"},
			{"F" , "unlv_tilde_crunching","1","Mark v.bad words for tilde crunch"},
			{"F" , "unrecognised_char","","Output char for unidentified blobs"},
			{"F" , "use_ambigs_for_adaption","0","Use ambigs for deciding whether to adapt to a character"},
			{"F" , "use_definite_ambigs_for_classifier","0","Use definite ambiguities when running character classifier"},
			{"F" , "use_new_state_cost","0","use new state cost heuristics for segmentation state evaluation"},
			{"F" , "use_only_first_uft8_step","0","Use only the first UTF8 step of the given string when computing log probabilities."},
			{"F" , "user_patterns_file","","A filename of user-provided patterns."},
			{"F" , "user_patterns_suffix","","A suffix of user-provided patterns located in tessdata."},
			{"F" , "user_words_file","","A filename of user-provided words."},
			{"F" , "user_words_suffix","","A suffix of user-provided words located in tessdata."},
			{"F" , "word_to_debug","","Word for which stopper debug information should be printed to stdout"},
			{"F" , "word_to_debug_lengths","","Lengths of unichars in word_to_debug"},
			{"F" , "wordrec_blob_pause","0","Blob pause"},
			{"F" , "wordrec_debug_blamer","0","Print blamer debug messages"},
			{"F" , "wordrec_debug_level","0","Debug level for wordrec"},
			{"F" , "wordrec_display_all_blobs","0","Display Blobs"},
			{"F" , "wordrec_display_all_words","0","Display Words"},
			{"F" , "wordrec_display_segmentations","0","Display Segmentations"},
			{"F" , "wordrec_display_splits","0","Display splits"},
			{"F" , "wordrec_enable_assoc","1","Associator Enable"},
			{"F" , "wordrec_max_join_chunks","4","Max number of broken pieces to associate"},
			{"F" , "wordrec_no_block","0","Don't output block information"},
			{"F" , "wordrec_run_blamer 0","","Try to set the blame for errors"},
			{"F" , "wordrec_skip_no_truth_words","0","Only run OCR for words that had truth recorded in BlamerBundle"},
			{"F" , "wordrec_worst_state","1","Worst segmentation state"},
			{"F" , "words_default_fixed_limit","0.6","Allowed size variance"},
			{"F" , "words_default_fixed_space","0.75","Fraction of xheight"},
			{"F" , "words_default_prop_nonspace","0.25","Fraction of xheight"},
			{"F" , "words_initial_lower","0.5","Max initial cluster size"},
			{"F" , "words_initial_upper","0.15","Min initial cluster spacing"},
			{"F" , "x_ht_acceptance_tolerance","8","Max allowed deviation of blob top outside of font data"},
			{"F" , "x_ht_min_change","8","Min change in xht before actually trying it"},
			{"F" , "xheight_penalty_inconsistent","0.25","Score penalty (0.1 = 10%) added if an xheight is inconsistent."},
			{"F" , "xheight_penalty_subscripts","0.125","Score penalty (0.1 = 10%) added if there are subscripts or superscripts in a word"}

			}
			;


	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }

	//
	//---------------------------------------------------------------------------------
	public cmcTesseractParameterDAO(cmcProcSettings iS , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = iS;
		logger = ilog;
	}

	//---------------------------------------------------------------------------------
	public ArrayList<cmcTesseractParameter> readAllParameters()
	//---------------------------------------------------------------------------------
	{
		// read file - if not there dump all parameters from memory onto a file and read the file
		String FName = xMSet.getTesseractOptionFileName();
		if( xMSet.xU.IsBestand( FName ) == false ) {
			createTesseractOptionFileName();
			if( xMSet.xU.IsBestand( FName ) == false ) {
				do_error("Cannot create Tesseract Option file [" + FName + "]");
			    return null;
			}    
		}
	    //    		
		return readTesseractOptionFile(false);
	}
	
	//---------------------------------------------------------------------------------
	public ArrayList<cmcTesseractParameter> makeDefaultList(boolean onlyWithold)
	//---------------------------------------------------------------------------------
	{
		ArrayList<cmcTesseractParameter> plist = new ArrayList<cmcTesseractParameter>();
		for(int i=0;i<lijst.length;i++)
		{
			cmcTesseractParameter x = new cmcTesseractParameter(lijst[i][1],lijst[i][2],lijst[i][3]);
			String wh = (lijst[i][0]==null) ? "FALSE" :lijst[i][0];
			boolean ib = wh.trim().toUpperCase().startsWith("T");
			x.setWithold(ib);
			if( (onlyWithold==true) && (ib==false) ) continue;
			plist.add(x);
		}
		return plist;
	}

	//---------------------------------------------------------------------------------
	private boolean createTesseractOptionFileName()
	//---------------------------------------------------------------------------------
	{
		return overWriteTesseractOptionFile( makeDefaultList(false) );
	}

	//---------------------------------------------------------------------------------
	public boolean overWriteTesseractOptionFile(ArrayList<cmcTesseractParameter> plist )
	//---------------------------------------------------------------------------------
	{
		String FName = xMSet.getTesseractOptionFileName();
		gpPrintStream pout = new gpPrintStream( FName , xMSet.getCodePageString());
		//
		pout.println (xMSet.getXMLEncodingHeaderLine());
		pout.println ("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		pout.println ("<!-- File Created: " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		pout.println ("<!-- Overview of the possible Tesseract Options -->" );
	    
        //
		pout.println("<TesseractOptionList>");
		pout.println("<![CDATA[");
	    for(int i=0;i<plist.size();i++)
	    {
	       String s= "<Option>"; 
	       s += "<Withold>" + plist.get(i).getWithold() + "</Withold>";
	       s += "<Parameter>" + plist.get(i).getParameter() + "</Parameter>";
	       s += "<Value>" + plist.get(i).getValue() + "</Value>";
	       s += "<Description>" + plist.get(i).getDescription() + "</Description>";
	       s += "</Option>";
	       pout.println( s );
	    }
		pout.println("]]>");
	    pout.println("</TesseractOptionList>");
		pout.close();
		return true;
	}

	//---------------------------------------------------------------------------------
	public ArrayList<cmcTesseractParameter> readActiveTesseractOptionFile()
	//---------------------------------------------------------------------------------
	{
		return readTesseractOptionFile(true);
	}
	
	//---------------------------------------------------------------------------------
	private ArrayList<cmcTesseractParameter> readTesseractOptionFile(boolean onlyWithold)
	//---------------------------------------------------------------------------------
	{
		String FName = xMSet.getTesseractOptionFileName();
		if( xMSet.xU.IsBestand( FName ) == false ) {
			do_error("Tesseract Option file cannot be found [" + FName + "]");
			return null;
		}
        //
		ArrayList<cmcTesseractParameter> plist = new ArrayList<cmcTesseractParameter>();
		ArrayList<cmcTesseractParameter> qlist = new ArrayList<cmcTesseractParameter>();

		BufferedReader reader = null;
		int nlines=0;
		try {
			File inFile  = new File(FName);  // File to read from.
	  	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	cmcTesseractParameter prm = null;
	    	String sVal = "";
		    while ((sLijn=reader.readLine()) != null) {
		    	sVal=null;
		    	nlines++;
	       		if( sLijn.indexOf("<Option>") >= 0 )  {
	        		prm = new cmcTesseractParameter(null,null,null);
	        	}
	            //
	            sVal = xMSet.xU.extractXMLValueV2(sLijn,"Withold"); 
	    		if ((sVal != null) && (prm!=null)) {prm.setWithold(xMSet.xU.ValueInBooleanValuePair("x=" + sVal.trim()));}
	    	    //
	    	   	sVal = xMSet.xU.extractXMLValueV2(sLijn,"Parameter"); 
	    		if ((sVal != null) && (prm!=null)) { prm.setParameter( sVal.trim());}
	    	    //
	    	   	sVal = xMSet.xU.extractXMLValueV2(sLijn,"Value"); 
	    		if ((sVal != null) && (prm!=null)) { prm.setValue( sVal.trim());}
	    	    //
	    	   	sVal = xMSet.xU.extractXMLValueV2(sLijn,"Description"); 
	    		if ((sVal != null) && (prm!=null)) { prm.setDescription( sVal.trim());}
	    	    //
	    		if( sLijn.indexOf("</Option>") >= 0 )  {
	    			if( prm == null ) continue;
	    			if( (prm.getWithold()==false) && (onlyWithold==true)) continue;
	    			if( (prm.getParameter()==null)||(prm.getValue()==null)||(prm.getDescription()==null) ) {
	    				do_error("Incomplete tesseractObject [" + prm.getWithold() + "][" + prm.getParameter() + "][" + prm.getValue() + "][" + prm.getDescription() + "]" );
	    				prm=null;
	    			    continue;   			
	    			}
	    	        if( prm.getWithold() ) plist.add( prm );
	    	                          else qlist.add(prm);
	        		//do_log(9,"[" + prm.getWithold() + "][" + prm.getParameter() + "][" + prm.getValue() + "][" + prm.getDescription() + "]");
	        	}
	       	}
	       	reader.close();
	       	reader=null;
		}   	
		catch(Exception e ) {
			do_error("Cannot read Tesseract Option file [" + FName + "]" + e.getMessage());
			return null;
		}
        finally {
           if( reader != null ) {
        	   try { reader.close(); } catch( Exception e ) { do_error("Error in finally"); }
           }
           int nn = (plist == null) ? 0 : plist.size();
           do_log(5,"Read [" + nn + "] records from [" + FName + "] [#Lines=" + nlines + "]");
        }
		// merge plist and qlist
		for(int i=0;i<qlist.size();i++)
		{
		   plist.add( qlist.get(i));	
		}
		qlist = null;
		return plist;
	}
	
	
}
