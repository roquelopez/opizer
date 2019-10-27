/*  1:   */ package com.opinosis;
/*  2:   */ 
/*  3:   */ import java.util.Comparator;
/*  4:   */ 
/*  5:   */ public class SummarySorter
/*  6:   */   implements Comparator<Candidate>
/*  7:   */ {
/*  8:   */   public int compare(Candidate s1, Candidate s2)
/*  9:   */   {
/* 10:12 */     if (s1.gain > s2.gain) {
/* 11:13 */       return -1;
/* 12:   */     }
/* 13:16 */     if (s1.gain < s2.gain) {
/* 14:17 */       return 1;
/* 15:   */     }
/* 16:20 */     return 0;
/* 17:   */   }
/* 18:   */ }


