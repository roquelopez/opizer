/*  1:   */ package com.opinosis;
/*  2:   */ 
/*  3:   */ import java.util.List;
/*  4:   */ 
/*  5:   */ public class Candidate
/*  6:   */   implements Comparable<Candidate>
/*  7:   */ {
/*  8: 8 */   public boolean discard = false;
/*  9:   */   public double gain;
/* 10:10 */   int level = 0;
/* 11:   */   public double localgain;
/* 12:   */   public double overlap;
/* 13:   */   public double rawscore;
/* 14:   */   public String sent;
/* 15:   */   public List<int[]> sentList;
/* 16:   */   public List<Node> theNodeList;
/* 17:   */   
/* 18:   */   public Candidate(double ogain, String sentence, List<int[]> sentList, int level)
/* 19:   */   {
/* 20:21 */     this.gain = ogain;
/* 21:22 */     this.sent = sentence;
/* 22:23 */     this.sentList = sentList;
/* 23:24 */     this.level = level;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Candidate(double overallGain, String str, List<int[]> overlapList, int level, double score, double gain)
/* 27:   */   {
/* 28:29 */     this.gain = overallGain;
/* 29:30 */     this.sent = str;
/* 30:31 */     this.sentList = overlapList;
/* 31:32 */     this.level = level;
/* 32:33 */     this.rawscore = score;
/* 33:34 */     this.localgain = gain;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public boolean equals(Object b)
/* 37:   */   {
/* 38:40 */     Candidate infob = (Candidate)b;
/* 39:42 */     if (this.sent.equals(infob.sent)) {
/* 40:42 */       return true;
/* 41:   */     }
/* 42:43 */     return false;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public int hashCode()
/* 46:   */   {
/* 47:48 */     return this.sent.hashCode();
/* 48:   */   }
/* 49:   */   
/* 50:   */   public int compareTo(Candidate info)
/* 51:   */   {
/* 52:54 */     List<int[]> sentList2 = info.sentList;
/* 53:   */     
/* 54:56 */     double overlap = Node.getSetenceJaccardOverlap(this.sentList, sentList2);
/* 55:58 */     if (((int[])sentList2.get(0))[0] == ((int[])this.sentList.get(0))[0])
/* 56:   */     {
/* 57:59 */       if (this.sentList.size() > sentList2.size()) {
/* 58:60 */         return 1;
/* 59:   */       }
/* 60:63 */       if (this.sentList.size() < sentList2.size()) {
/* 61:64 */         return -1;
/* 62:   */       }
/* 63:66 */       return 0;
/* 64:   */     }
/* 65:69 */     if (((int[])sentList2.get(0))[0] > ((int[])this.sentList.get(0))[0]) {
/* 66:70 */       return 1;
/* 67:   */     }
/* 68:73 */     return -1;
/* 69:   */   }
/* 70:   */ }


