/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import java.util.HashMap;
/*   4:    */ import java.util.HashSet;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public class WordInfo
/*   8:    */ {
/*   9: 10 */   HashMap<String, Node> hm = new HashMap();
/*  10: 12 */   int totalWords = 0;
/*  11: 13 */   int mMaxOccurence = 0;
/*  12:    */   
/*  13:    */   public int getTotalWordCount()
/*  14:    */   {
/*  15: 21 */     return this.totalWords;
/*  16:    */   }
/*  17:    */   
/*  18:    */   public int getWordCount(String str)
/*  19:    */   {
/*  20: 33 */     throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from double to int\n");
/*  21:    */   }
/*  22:    */   
/*  23:    */   public List<Integer> getSentences(String str)
/*  24:    */   {
/*  25: 46 */     throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from HashSet<Integer> to List<Integer>\n");
/*  26:    */   }
/*  27:    */   
/*  28:    */   public int getWordPos(String word, int sid)
/*  29:    */   {
/*  30: 54 */     throw new Error("Unresolved compilation problem: \n\tThe method getPosition(int) is undefined for the type Node\n");
/*  31:    */   }
/*  32:    */   
/*  33:    */   public int getSentencesCount(String str)
/*  34:    */   {
/*  35: 60 */     Node node = (Node)this.hm.get(str);
/*  36: 61 */     if (node != null) {
/*  37: 63 */       return Node.sentences.size();
/*  38:    */     }
/*  39: 66 */     return -1;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void incrementCount(String str, int sid, int pos)
/*  43:    */   {
/*  44: 77 */     throw new Error("Unresolved compilation problems: \n\tThe method updateNodeCount(int, int) is undefined for the type Node\n\tThe method updateNodeCount(int, int) is undefined for the type Node\n\tThe method setMaxOccurence(int) in the type WordInfo is not applicable for the arguments (double)\n\tE cannot be resolved to a type\n");
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int getMaxOccurence()
/*  48:    */   {
/*  49: 95 */     return this.mMaxOccurence;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void setMaxOccurence(int maxOccurence)
/*  53:    */   {
/*  54:105 */     if (this.mMaxOccurence < maxOccurence) {
/*  55:106 */       this.mMaxOccurence = maxOccurence;
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void setInvalidNode(String str)
/*  60:    */   {
/*  61:115 */     throw new Error("Unresolved compilation problem: \n\tThe method setValidNode(boolean) is undefined for the type Node\n");
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean isNodeValid(String str)
/*  65:    */   {
/*  66:134 */     throw new Error("Unresolved compilation problem: \n\tThe method isValidNode() is undefined for the type Node\n");
/*  67:    */   }
/*  68:    */ }


