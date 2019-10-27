/*    1:     */ package com.opinosis;
/*    2:     */ 
/*    3:     */ 
/*    4:     */ import java.io.IOException;
/*    5:     */ import java.io.Writer;
/*    6:     */ import java.util.HashMap;
/*    7:     */ import java.util.HashSet;
/*    8:     */ import java.util.List;
/*    9:     */ import java.util.Set;
/*   10:     */ import org.jgrapht.graph.DefaultWeightedEdge;
/*   11:     */ import org.jgrapht.graph.SimpleDirectedGraph;
/*   12:     */ import org.jgrapht.graph.SimpleDirectedWeightedGraph;
/*   13:     */ 
/*   14:     */ public class OpinosisCandidateGen
/*   15:     */   
/*   16:     */ {
/*   17:     */   static int clusterID;
/*   18:     */   static int oldClusterID;
/*   19:     */   String beforeAttach;
/*   20:     */   double beforeAttachGain;
/*   21:     */   double beforeAttachScore;
/*   22:     */   private int originalLevel;
/*   23:     */   Writer print;
/*   24:     */   HashSet<Candidate> shortlisted;
/*   25:     */   HashMap<String, Candidate> tempCollapsed;
/*   26:     */   HashMap<String, Node> wordNodeMap;
/*   27:     */   public OpinosisCandidateGen(SimpleDirectedGraph<Node, DefaultWeightedEdge> g, WordInfo wrdInfo, Writer writer) {}
/*   28:     */   
/*   29:     */   public OpinosisCandidateGen(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer) {}
/*   30:     */   
/*   31:     */   class Topic
/*   32:     */     implements Comparable<Topic>
/*   33:     */   {
/*   34:     */     String name;
/*   35:     */     List<int[]> sids;
/*   36:     */     
/*   37:     */     public Topic(List<int[]> overlapList) {}
/*   38:     */     
/*   39:     */     public int compareTo(Topic t)
/*   40:     */     {
/*   41:  76 */       throw new Error("Unresolved compilation problem: \n");
/*   42:     */     }
/*   43:     */   }
/*   44:     */   
/*   45:     */   private double computeAdjustedScore(boolean concatOn, int level, double pathProb, int intersect)
/*   46:     */   {
/*   47: 103 */     throw new Error("Unresolved compilation problem: \n");
/*   48:     */   }
/*   49:     */   
/*   50:     */   private boolean doCheck(int level, boolean concatOn)
/*   51:     */   {
/*   52: 122 */     throw new Error("Unresolved compilation problem: \n\tP_MIN_SENT_LENGTH cannot be resolved\n");
/*   53:     */   }
/*   54:     */   
/*   55:     */   private boolean doCollapse(Node x, List<int[]> YintersectX, double pathscore, double prevPathScore, String str, List<int[]> overlapList, int level, boolean concatOn)
/*   56:     */   {
/*   57: 149 */     throw new Error("Unresolved compilation problems: \n\tmGraph2 cannot be resolved\n\tmGraph2 cannot be resolved\n\tCONFIG_MIN_REDUNDANCY cannot be resolved\n");
/*   58:     */   }
/*   59:     */   
/*   60:     */   public void generateClusters()
/*   61:     */     throws IOException
/*   62:     */   {
/*   63: 195 */     throw new Error("Unresolved compilation problem: \n\tmGraph2 cannot be resolved\n");
/*   64:     */   }
/*   65:     */   
/*   66:     */   private double getCurrentGain(double gain, double r, int level)
/*   67:     */   {
/*   68: 266 */     throw new Error("Unresolved compilation problems: \n\tCONFIG_SCORING_FUNCTION cannot be resolved\n\tGAIN_REDUNDANCY_ONLY cannot be resolved\n\tCONFIG_SCORING_FUNCTION cannot be resolved\n\tGAIN_WEIGHTED_REDUNDANCY_BY_LEVEL cannot be resolved\n\tCONFIG_SCORING_FUNCTION cannot be resolved\n\tGAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL cannot be resolved\n");
/*   69:     */   }
/*   70:     */   
/*   71:     */   private List<Candidate> getFinalSentences()
/*   72:     */   {
/*   73: 302 */     throw new Error("Unresolved compilation problems: \n\tDEBUG cannot be resolved\n\tCONFIG_MAX_SUMMARIES cannot be resolved\n\tCONFIG_MAX_SUMMARIES cannot be resolved\n");
/*   74:     */   }
/*   75:     */   
/*   76:     */   private double getGreedyWeight(Node y)
/*   77:     */   {
/*   78: 348 */     throw new Error("Unresolved compilation problem: \n");
/*   79:     */   }
/*   80:     */   
/*   81:     */   private List<Node> getNodeList(String sent)
/*   82:     */   {
/*   83: 396 */     throw new Error("Unresolved compilation problem: \n");
/*   84:     */   }
/*   85:     */   
/*   86:     */   private double getOverallGain(double score, int level)
/*   87:     */   {
/*   88: 426 */     throw new Error("Unresolved compilation problems: \n\tCONFIG_NORMALIZE_OVERALLGAIN cannot be resolved\n\tCONFIG_USE_POS_GAIN cannot be resolved\n");
/*   89:     */   }
/*   90:     */   
/*   91:     */   private double getPathProb(Node x, Node y, double score, List l)
/*   92:     */   {
/*   93: 441 */     throw new Error("Unresolved compilation problem: \n");
/*   94:     */   }
/*   95:     */   
/*   96:     */   public double computeCandidateSimScore(Candidate s1, Candidate s2)
/*   97:     */   {
/*   98: 449 */     throw new Error("Unresolved compilation problem: \n");
/*   99:     */   }
/*  100:     */   
/*  101:     */   private boolean isValidEnd(Node x, List<int[]> overlapList, int level, double gain, String str, boolean concatOn, boolean overlapSame)
/*  102:     */   {
/*  103: 495 */     throw new Error("Unresolved compilation problem: \n\tmGraph2 cannot be resolved\n");
/*  104:     */   }
/*  105:     */   
/*  106:     */   private boolean isValidSentence(Node x, List<int[]> overlapList, int level, double totalgain, String str)
/*  107:     */   {
/*  108: 503 */     throw new Error("Unresolved compilation problem: \n");
/*  109:     */   }
/*  110:     */   
/*  111:     */   private boolean matchesEndPattern(String str)
/*  112:     */   {
/*  113: 567 */     throw new Error("Unresolved compilation problem: \n");
/*  114:     */   }
/*  115:     */   
/*  116:     */   private void print()
/*  117:     */   {
/*  118: 581 */     throw new Error("Unresolved compilation problem: \n");
/*  119:     */   }
/*  120:     */   
/*  121:     */   private void print(String str)
/*  122:     */   {
/*  123: 586 */     throw new Error("Unresolved compilation problem: \n");
/*  124:     */   }
/*  125:     */   
/*  126:     */   private void println(String str)
/*  127:     */   {
/*  128: 590 */     throw new Error("Unresolved compilation problem: \n");
/*  129:     */   }
/*  130:     */   
/*  131:     */   private boolean processFound()
/*  132:     */   {
/*  133: 593 */     throw new Error("Unresolved compilation problem: \n");
/*  134:     */   }
/*  135:     */   
/*  136:     */   private void processNext(Node x, String str, Set<DefaultWeightedEdge> outgoing, List<int[]> overlapList, double pathscore, int level, boolean concatOn)
/*  137:     */   {
/*  138: 670 */     throw new Error("Unresolved compilation problems: \n\tmGraph2 cannot be resolved\n\tCONFIG_TURN_ON_COLLAPSE cannot be resolved\n\tCONFIG_ATTACHMENT_AFTER cannot be resolved\n");
/*  139:     */   }
/*  140:     */   
/*  141:     */   private Candidate remove(Candidate currSentence, Candidate best)
/*  142:     */   {
/*  143: 706 */     throw new Error("Unresolved compilation problem: \n");
/*  144:     */   }
/*  145:     */   
/*  146:     */   private HashSet<Candidate> removeDuplicates(HashSet<Candidate> set, boolean isIntermediate)
/*  147:     */   {
/*  148: 798 */     throw new Error("Unresolved compilation problems: \n\tCONFIG_TURN_ON_DUP_ELIM cannot be resolved\n\tDEBUG cannot be resolved\n\tThe method getSentenceJaccardOverlap(Candidate, Candidate) is undefined for the type OpinosisCandidateGen\n\tCONFIG_DUPLICATE_COLLAPSE_THRESHOLD cannot be resolved\n\tCONFIG_DUPLICATE_THRESHOLD cannot be resolved\n");
/*  149:     */   }
/*  150:     */   
/*  151:     */   private Candidate removeFinal(Candidate currSentence, Candidate best)
/*  152:     */   {
/*  153: 907 */     throw new Error("Unresolved compilation problem: \n");
/*  154:     */   }
/*  155:     */   
/*  156:     */   private boolean shouldContinue(Node x, List<int[]> overlapList, int level, double score, String str)
/*  157:     */   {
/*  158: 937 */     throw new Error("Unresolved compilation problems: \n\tP_MAX_SENT_LENGTH cannot be resolved\n\tCONFIG_MIN_REDUNDANCY cannot be resolved\n");
/*  159:     */   }
/*  160:     */   
/*  161:     */   private boolean traverse2(Node x, List<int[]> overlapList, String str, double pathscore, int level, boolean concatOn, boolean overlapSame)
/*  162:     */   {
/*  163:1013 */     throw new Error("Unresolved compilation problem: \n\tmGraph2 cannot be resolved\n");
/*  164:     */   }
/*  165:     */ }

