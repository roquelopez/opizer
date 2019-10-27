/*   1:    */ package com.opinosis.summarizer;
/*   2:    */ 
/*   3:    */ import com.opinosis.Candidate;
/*   4:    */ import com.opinosis.Node;
/*   5:    */ import com.opinosis.OpinosisCore;
/*   6:    */ import com.opinosis.OpinosisSettings;
/*   7:    */ import java.io.Writer;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.HashMap;
/*  10:    */ import java.util.HashSet;
/*  11:    */ import java.util.List;
/*  12:    */ import org.jgrapht.graph.DefaultWeightedEdge;
/*  13:    */ import org.jgrapht.graph.SimpleDirectedGraph;
/*  14:    */ import org.jgrapht.graph.SimpleDirectedWeightedGraph;
/*  15:    */ import org.textbug.utility.MathUtil;
/*  16:    */ 
/*  17:    */ public class BasicSummarizer
/*  18:    */   extends OpinosisCore
/*  19:    */ {
/*  20:    */   public BasicSummarizer(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer)
/*  21:    */   {
/*  22: 23 */     super(g, wordNodeMap, printer);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public boolean isValidCandidate(String str)
/*  26:    */   {
	System.out.println(str);
//Original Rules
/*  27: 32 */     boolean isGood = false;
/*  28: 34 */     if (str.matches(".*(/adj)*.*(/n)+.*(/v)+.*(/adj)+.*")) {//Rule 1
/*  29: 35 */       isGood = true;
/*  30: 38 */     } else if ((!str.matches(".*(/art).*")) && (str.matches(".*(/adv)*.*(/adj)+.*(/n)+.*"))) {//Rule 3
/*  31: 39 */       isGood = true;
/*  32: 42 */     } else if (str.matches(".*(/propess|/art)+.*(/v)+.*(/adv|/adj)+.*(/n)+.*")) {//Rule 1 modified
/*  33: 43 */       isGood = true;
/*  34: 46 */     //} else if (str.matches(".*(/adj)+.*(/to)+.*(/v).*")) {// Rule 2
/*  35: 47 */       //isGood = true;
/*  36: 50 */     } else if (str.matches(".*(/adv)+.*(/ks|/prep)+.*(/n)+.*")) {//Rule 4
/*  37: 51 */       isGood = true;
/*  38:    */     }
/*  39: 82 */     String last = str.substring(str.lastIndexOf(' '), str.length());
/*  40: 83 */     if (last.matches(".*(/ks|/prep|/kc|/propess|/art|/,)")) {
/*  41: 84 */       isGood = false;
/*  42:    */     }
/*  43: 87 */     return isGood;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public List<int[]> getNodeOverlap(List<int[]> left, List<int[]> right)
/*  47:    */   {
/*  48:115 */     List<int[]> l3 = new ArrayList();
/*  49:    */     
/*  50:117 */     int pointer = 0;
/*  51:119 */     for (int i = 0; i < left.size(); i++)
/*  52:    */     {
/*  53:120 */       int[] eleft = (int[])left.get(i);
/*  54:122 */       if (pointer > right.size()) {
/*  55:    */         break;
/*  56:    */       }
/*  57:126 */       for (int j = pointer; j < right.size(); j++)
/*  58:    */       {
/*  59:128 */         int[] eright = (int[])right.get(j);
/*  60:130 */         if (eright[0] == eleft[0])
/*  61:    */         {
/*  62:134 */           if ((eright[1] > eleft[1]) && (Math.abs(eright[1] - eleft[1]) <= OpinosisSettings.CONFIG_PERMISSABLE_GAP))
/*  63:    */           {
/*  64:136 */             l3.add(eright);
/*  65:137 */             pointer = j + 1;
/*  66:138 */             break;
/*  67:    */           }
/*  69:    */         }
/*  70:    */         else
/*  71:    */         {
/*  72:148 */           if (eright[0] > eleft[0]) {
/*  73:    */             break;
/*  74:    */           }
/*  75:    */         }
/*  76:    */       }
/*  77:    */     }
/*  78:155 */     return l3;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean isVSN(Node x)
/*  82:    */   {
/*  83:161 */     String nname = x.getNodeName();
/*  84:163 */     if (x.getAveragePos() <= 15.0D) {
/*  85:165 */       if ((nname.contains("/adj")) || 
/*  86:166 */         (nname.contains("/adv")) || 
/*  87:167 */         (nname.contains("/propess$")) || 
/*  88:168 */         (nname.contains("/n")) || 
/*  90:170 */         (nname.contains("/art")) || 
/*  91:171 */         (nname.matches("^(seu/|sua/|quando/|um/|uma/|o/|a/|os/|as/|ele/|ela/|eles/|elas/|esse/|essa|eu/|nós/|nosso/|nossa/).*")) || 
/*  92:172 */         (nname.contains("o/propess")) || 
/*  93:173 */         (nname.contains("se/")) || 
/*  94:174 */         (nname.contains("para/"))) {
/*  95:175 */         return true;
/*  96:    */       }
/*  97:    */     }
/*  98:176 */     return false;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public boolean isVEN(Node x, int pathLength, boolean isCollapsedCandidate)
/* 102:    */   {
/* 103:190 */     if (isEndToken(x)) {
/* 104:191 */       return true;
/* 105:    */     }
/* 106:195 */     if (getGraph().outDegreeOf(x) <= 0) {
/* 107:196 */       return true;
/* 108:    */     }
/* 109:200 */     return false;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public boolean isEndToken(Node x)
/* 113:    */   {
/* 114:212 */     String token = x.getNodeName();
/* 115:214 */     if (token.matches(".*(/\\.|/,)")) {
/* 116:215 */       return true;
/* 117:    */     }
/* 118:218 */     return false;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public double computeCandidateSimScore(Candidate s1, Candidate s2)
/* 122:    */   {
/* 123:236 */     List<Node> l1 = s1.theNodeList;
/* 124:237 */     List<Node> l2 = s2.theNodeList;
/* 125:    */     
/* 126:239 */     HashSet union = new HashSet(l1);
/* 127:240 */     HashSet intersect = new HashSet(l1);
/* 128:    */     
/* 129:242 */     union.addAll(l2);
/* 130:243 */     intersect.retainAll(l2);
/* 131:    */     
/* 132:245 */     double overlap = intersect.size() / union.size();
/* 133:    */     
/* 134:    */ 
/* 135:248 */     return overlap;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public double computeScore(double currentScore, List<int[]> currOverlapList, int pathLength)
/* 139:    */   {
/* 140:257 */     double theGain = 0.0D;
/* 141:258 */     int overlapSize = currOverlapList.size();
/* 142:260 */     if (CONFIG_SCORING_FUNCTION == GAIN_REDUNDANCY_ONLY) {
/* 143:261 */       theGain = currentScore + overlapSize;
/* 144:    */     }
/* 145:264 */     if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LEVEL) {
/* 146:265 */       theGain = currentScore + overlapSize * pathLength;
/* 147:    */     }
/* 148:268 */     if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL) {
/* 149:269 */       if (pathLength > 1) {
/* 150:270 */         theGain = currentScore + overlapSize * MathUtil.getLog2(pathLength);
/* 151:    */       } else {
/* 152:273 */         theGain = currentScore + overlapSize;
/* 153:    */       }
/* 154:    */     }
/* 155:276 */     return theGain;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public double computeAdjustedScore(double score, int level)
/* 159:    */   {
/* 160:283 */     double oGain = score;
/* 161:285 */     if (CONFIG_NORMALIZE_OVERALLGAIN) {
/* 162:286 */       oGain /= level;
/* 163:    */     }
/* 164:289 */     return oGain;
/* 165:    */   }
/* 166:    */   
/* 167:    */   public boolean shouldContinueTraverse(Node x, List<int[]> overlapSoFar, int pathLength, double score)
/* 168:    */   {
/* 169:297 */     if (pathLength >= this.P_MAX_SENT_LENGTH) {
/* 170:298 */       return false;
/* 171:    */     }
/* 172:301 */     if (score == (-1.0D / 0.0D)) {
/* 173:302 */       return false;
/* 174:    */     }
/* 175:305 */     if ((overlapSoFar.size() < CONFIG_MIN_REDUNDANCY) && 
/* 176:306 */       (!isEndToken(x))) {
/* 177:307 */       return false;
/* 178:    */     }
/* 179:310 */     return true;
/* 180:    */   }
/* 181:    */ }

