/*  1:   */ package com.opinosis;
/*  2:   */ 
/*  3:   */ public class OpinosisSettings
/*  4:   */ {
/*  5: 5 */   public static final String FILE_SEP = System.getProperty("file.separator");
/*  6:   */   static final String FILE_KAVITA = "E:\\Projects\\MyTopiq\\data\\evaluation\\txt.data.parsed";
/*  7: 8 */   public static int GAIN_REDUNDANCY_ONLY = 1;
/*  8: 9 */   public static int GAIN_WEIGHTED_REDUNDANCY_BY_LEVEL = 2;
/*  9:10 */   public static int GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL = 3;
/* 10:   */   static final String CURRENT_FILE = "E:\\Projects\\MyTopiq\\data\\evaluation\\txt.data.parsed";
/* 11:19 */   public double P_TOPIC_THRESHOLD = 0.1D;
/* 12:21 */   public double P_SENTENCE_THRESHOLD = 0.05D;
/* 13:28 */   public int P_MAX_SENT_LENGTH = 18;
/* 14:31 */   public int P_MIN_SENT_LENGTH = 2;
/* 15:   */   @Deprecated
/* 16:35 */   public int P_MIN_TOPIC_OVERLAP = 1;
/* 17:40 */   public static boolean CONFIG_TURN_ON_COLLAPSE = false;
/* 18:42 */   public static boolean CONFIG_TURN_ON_DUP_ELIM = false;
/* 19:44 */   public static boolean CONFIG_NORMALIZE_OVERALLGAIN = true;
/* 20:46 */   public static int CONFIG_MIN_REDUNDANCY = 2;
/* 21:48 */   public static int CONFIG_PERMISSABLE_GAP = 4;
/* 22:50 */   public static int CONFIG_ATTACHMENT_AFTER = 2;
/* 23:52 */   public static boolean CONFIG_USE_POS_GAIN = false;
/* 24:54 */   public static double CONFIG_MAX_SUMMARIES = 5.0D;
/* 25:56 */   public static double CONFIG_DUPLICATE_THRESHOLD = 0.35D;
/* 26:58 */   public static int CONFIG_SCORING_FUNCTION = GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL;
/* 27:60 */   public static double CONFIG_DUPLICATE_COLLAPSE_THRESHOLD = 0.5D;
/* 28:   */ }