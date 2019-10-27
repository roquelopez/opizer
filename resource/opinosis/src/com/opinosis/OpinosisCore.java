/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.PrintStream;
/*   5:    */ import java.io.Writer;
/*   6:    */ import java.util.ArrayList;
/*   7:    */ import java.util.Collection;
/*   8:    */ import java.util.Collections;
/*   9:    */ import java.util.HashMap;
/*  10:    */ import java.util.HashSet;
/*  11:    */ import java.util.Iterator;
/*  12:    */ import java.util.List;
/*  13:    */ import java.util.Set;
/*  14:    */ import org.jgrapht.graph.DefaultWeightedEdge;
/*  15:    */ import org.jgrapht.graph.SimpleDirectedGraph;
/*  16:    */ import org.jgrapht.graph.SimpleDirectedWeightedGraph;
/*  17:    */ 
/*  18:    */ public abstract class OpinosisCore
/*  19:    */   extends OpinosisSettings
/*  20:    */ {
/*  21:    */   SimpleDirectedGraph<Node, DefaultWeightedEdge> mGraph;
/*  22:    */   Writer mWriter;
/*  23: 25 */   HashMap<String, Node> mWordNodeMap = null;
/*  24:    */   protected static final boolean DEBUG = false;
/*  25: 30 */   String mAnchor = "";
/*  26: 31 */   double beforeAttachGain = 0.0D;
/*  27: 32 */   double mAnchorPathScore = 0.0D;
/*  28: 33 */   private int mAnchorPathLen = 0;
/*  29: 35 */   HashSet<Candidate> shortlisted = new HashSet();
/*  30: 36 */   HashMap<String, Candidate> ccList = new HashMap();
/*  31:    */   
/*  32:    */   private void print()
/*  33:    */   {
/*  34: 39 */     System.out.print("");
/*  35:    */   }
/*  36:    */   
/*  37:    */   private void print(String str)
/*  38:    */   {
/*  39: 44 */     System.out.print(str + " ");
/*  40:    */   }
/*  41:    */   
/*  42:    */   private void println(String str)
/*  43:    */   {
/*  44: 48 */     System.out.println(str + " ");
/*  45:    */   }
/*  46:    */   
/*  47:    */   public OpinosisCore(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer)
/*  48:    */   {
/*  49: 53 */     this.mGraph = g;
/*  50: 54 */     this.mWriter = printer;
/*  51: 55 */     this.mWordNodeMap = wordNodeMap;
/*  52:    */   }
/*  53:    */   
/*  54:    */   private boolean doCollapse(Node x, List<int[]> YintersectX, double pathscore, double prevPathScore, String str, List<int[]> overlapList, int level, boolean concatOn)
/*  55:    */   {
/*  56: 85 */     this.mAnchor = str;
/*  57: 86 */     this.mAnchorPathScore = prevPathScore;
/*  58: 87 */     this.mAnchorPathLen = level;
/*  59:    */     
/*  60: 89 */     Set<DefaultWeightedEdge> edges = this.mGraph.outgoingEdgesOf(x);
/*  61: 90 */     if ((edges != null) && (edges.size() > 1))
/*  62:    */     {
/*  63: 92 */       Iterator<DefaultWeightedEdge> cIter = edges.iterator();
/*  64: 93 */       while (cIter.hasNext())
/*  65:    */       {
/*  66: 95 */         DefaultWeightedEdge cEdgeOfX = (DefaultWeightedEdge)cIter.next();
/*  67: 96 */         Node cY = (Node)this.mGraph.getEdgeTarget(cEdgeOfX);
/*  68: 97 */         String cYNodeName = cY.getNodeName();
/*  69:    */         
/*  70:    */ 
/*  71:    */ 
/*  72:    */ 
/*  73:102 */         List<int[]> cYintersectX = getNodeOverlap(overlapList, cY.getSentenceIds());
/*  74:    */         
/*  75:    */ 
/*  76:105 */         int newLevel = level + 1;
/*  77:106 */         double newPathScore = computeScore(pathscore, cYintersectX, newLevel);
/*  78:109 */         if (cYintersectX.size() >= CONFIG_MIN_REDUNDANCY) {
/*  79:110 */           traverse(cY, cYintersectX, "xx " + cYNodeName, newPathScore, newLevel, true, false);
/*  80:    */         }
/*  81:    */       }
/*  82:    */     }
/*  83:113 */     concatOn = false;
/*  84:    */     
/*  85:    */ 
/*  86:    */ 
/*  87:    */ 
/*  88:118 */     return processFound();
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void start()
/*  92:    */     throws IOException
/*  93:    */   {
/*  94:129 */     long startTime = 0L;
/*  95:130 */     long endTime = 0L;
/*  96:    */     
/*  97:132 */     Set<Node> nodeList = this.mGraph.vertexSet();
/*  98:133 */     Iterator<Node> nodes = nodeList.iterator();
/*  99:134 */     while (nodes.hasNext())
/* 100:    */     {
/* 101:136 */       Node x = (Node)nodes.next();
/* 102:137 */       double score = 0.0D;
/* 103:138 */       if (isVSN(x)) {
/* 104:140 */         traverse(x, x.getSentenceIds(), x.getNodeName(), score, 1, false, false);
/* 105:    */       }
/* 106:    */     }
/* 107:144 */     List<Candidate> theSentenceInfos = getFinalSentences();
/* 108:149 */     for (Candidate info : theSentenceInfos)
/* 109:    */     {
/* 110:150 */       info.sent = info.sent.replaceAll("(/[a-z,.;$]+(\\s+|$))", " ");
/* 111:151 */       info.sent = info.sent.replaceAll("xx", "");
/* 112:152 */       info.sent += " .";
/* 113:153 */       info.sent = info.sent.replaceAll("\\s+", " ");
/* 114:154 */       this.mWriter.append(info.sent);
/* 115:    */       
/* 116:    */ 
/* 117:    */ 
/* 118:    */ 
/* 119:    */ 
/* 120:    */ 
/* 121:    */ 
/* 122:162 */       this.mWriter.append("\n");
/* 123:    */     }
/* 124:165 */     this.mWriter.close();
/* 125:    */   }
/* 126:    */   
/* 127:    */   private List<Candidate> getFinalSentences()
/* 128:    */   {
/* 129:175 */     List<Candidate> temp = new ArrayList();
/* 130:176 */     List<Candidate> shortlistedFinal = new ArrayList();
/* 131:178 */     if (this.shortlisted.size() <= 0) {
/* 132:179 */       return shortlistedFinal;
/* 133:    */     }
/* 134:181 */     temp.addAll(removeDuplicates(this.shortlisted, false));
/* 135:    */     
/* 136:183 */     Collections.sort(temp, new SummarySorter());
/* 137:193 */     if (temp.size() > CONFIG_MAX_SUMMARIES)
/* 138:    */     {
/* 139:194 */       shortlistedFinal.add((Candidate)temp.get(0));
/* 140:    */       
/* 141:196 */       int i = 1;
/* 142:    */       do
/* 143:    */       {
/* 144:197 */         Candidate a = (Candidate)temp.get(i - 1);
/* 145:198 */         Candidate b = (Candidate)temp.get(i);
/* 146:199 */         shortlistedFinal.add(b);i++;
/* 147:196 */         if (i >= temp.size()) {
/* 148:    */           break;
/* 149:    */         }
/* 150:196 */       } while (shortlistedFinal.size() < CONFIG_MAX_SUMMARIES);
/* 151:    */     }
/* 152:    */     else
/* 153:    */     {
/* 154:202 */       shortlistedFinal.addAll(temp);
/* 155:    */     }
/* 156:204 */     return shortlistedFinal;
/* 157:    */   }
/* 158:    */   
/* 159:    */   private List<Node> getNodeList(String sent)
/* 160:    */   {
/* 161:210 */     String[] tokens = sent.split("\\s+");
/* 162:211 */     ArrayList<Node> l = new ArrayList();
/* 163:213 */     for (String token : tokens) {
/* 164:215 */       if (token.matches(".*(/n|/adj|/v[a-s]).*"))
/* 165:    */       {
/* 166:217 */         Node n = (Node)this.mWordNodeMap.get(token);
/* 167:219 */         if (n != null) {
/* 168:220 */           l.add(n);
/* 169:    */         }
/* 170:    */       }
/* 171:    */     }
/* 172:226 */     return l;
/* 173:    */   }
/* 174:    */   
/* 175:    */   private boolean processFound()
/* 176:    */   {
/* 177:233 */     boolean success = false;
/* 178:234 */     Collection<Candidate> temp = this.ccList.values();
/* 179:235 */     HashSet<Candidate> collapsed = new HashSet(temp);
/* 180:    */     
/* 181:237 */     collapsed = removeDuplicates(collapsed, true);
/* 182:    */     
/* 183:239 */     int i = 0;
/* 184:240 */     if (collapsed.size() > 1)
/* 185:    */     {
/* 186:242 */       double overallgains = 0.0D;
/* 187:243 */       double allscores = this.mAnchorPathScore;
/* 188:244 */       double allgains = this.beforeAttachGain;
/* 189:245 */       int alllevels = this.mAnchorPathLen;
/* 190:    */       
/* 191:247 */       StringBuffer buffer = new StringBuffer(this.mAnchor);
/* 192:248 */       List<int[]> sentList = new ArrayList();
/* 193:250 */       for (Candidate theInfo : collapsed)
/* 194:    */       {
/* 195:255 */         overallgains += theInfo.gain;
/* 196:256 */         allgains += theInfo.localgain;
/* 197:257 */         allscores += theInfo.rawscore;
/* 198:258 */         alllevels += theInfo.level;
/* 199:259 */         sentList.addAll(theInfo.sentList);
/* 200:260 */         if ((i > 0) && (i == collapsed.size() - 1)) {
/* 201:261 */           buffer.append(" e ");
/* 202:263 */         } else if (i > 0) {
/* 203:264 */           buffer.append(" , ");
/* 204:    */         } else {
/* 205:267 */           buffer.append(" ");
/* 206:    */         }
/* 207:269 */         buffer.append(theInfo.sent);
/* 208:270 */         i++;
/* 209:    */       }
/* 210:273 */       if (this.ccList.size() > 1)
/* 211:    */       {
/* 212:275 */         double overallGain = overallgains / this.ccList.size();
/* 213:    */         
/* 214:    */ 
/* 215:278 */         this.shortlisted.add(new Candidate(overallGain, buffer.toString(), sentList, alllevels));
/* 216:    */         
/* 217:280 */         success = true;
/* 218:    */       }
/* 219:    */     }
/* 220:287 */     this.ccList.clear();
/* 221:288 */     this.mAnchor = "";
/* 222:289 */     this.beforeAttachGain = 0.0D;
/* 223:290 */     this.mAnchorPathScore = 0.0D;
/* 224:291 */     this.mAnchorPathLen = 0;
/* 225:    */     
/* 226:293 */     return success;
/* 227:    */   }
/* 228:    */   
/* 229:    */   private void processNext(Node x, String str, List<int[]> overlapList, double currentPathScore, int pathLen, boolean isCollapsedPath)
/* 230:    */   {
/* 231:299 */     Set<DefaultWeightedEdge> outgoing = this.mGraph.outgoingEdgesOf(x);
/* 232:300 */     if ((outgoing != null) && (outgoing.size() > 0))
/* 233:    */     {
/* 234:302 */       Iterator<DefaultWeightedEdge> xEdges = outgoing.iterator();
/* 235:303 */       boolean doMore = true;
/* 236:305 */       while ((xEdges.hasNext()) && (doMore))
/* 237:    */       {
/* 238:307 */         DefaultWeightedEdge xEdge = (DefaultWeightedEdge)xEdges.next();
/* 239:    */         
/* 240:309 */         Node y = (Node)this.mGraph.getEdgeTarget(xEdge);
/* 241:310 */         String yNodeName = y.getNodeName();
/* 242:    */         
/* 243:312 */         List<int[]> currOverlapList = getNodeOverlap(overlapList, y.getSentenceIds());
/* 244:314 */         if (currOverlapList.size() > 0)
/* 245:    */         {
/* 246:316 */           int newPathLen = pathLen + 1;
/* 247:317 */           double newPathScore = computeScore(currentPathScore, currOverlapList, newPathLen);
/* 248:320 */           if ((CONFIG_TURN_ON_COLLAPSE) && (pathLen >= CONFIG_ATTACHMENT_AFTER) && (!isCollapsedPath) && (currOverlapList.size() <= overlapList.size()) && (x.getNodeName().matches(".*(/v[a-z]|/ks)")))
/* 249:    */           {
/* 250:323 */             boolean success = doCollapse(x, currOverlapList, newPathScore, currentPathScore, str, overlapList, pathLen, isCollapsedPath);
/* 251:325 */             if (!success)
/* 252:    */             {
/* 253:327 */               String strTemp = str + " " + y.getNodeName();
/* 254:328 */               doMore = traverse(y, currOverlapList, strTemp, newPathScore, newPathLen, isCollapsedPath, false);
/* 255:    */             }
/* 256:    */           }
/* 257:    */           else
/* 258:    */           {
/* 259:335 */             String strTemp = str + " " + yNodeName;
/* 260:336 */             doMore = traverse(y, currOverlapList, strTemp, newPathScore, pathLen + 1, isCollapsedPath, false);
/* 261:    */           }
/* 262:    */         }
/* 263:    */       }
/* 264:    */     }
/* 265:    */   }
/* 266:    */   
/* 267:    */   private Candidate remove(Candidate currSentence, Candidate best)
/* 268:    */   {
/* 269:343 */     double temp = currSentence.gain;
/* 270:345 */     if ((best.gain < currSentence.gain) && (best.level <= currSentence.level))
/* 271:    */     {
/* 272:347 */       best.discard = true;
/* 273:348 */       best = currSentence;
/* 274:    */     }
/* 275:    */     else
/* 276:    */     {
/* 277:352 */       currSentence.discard = true;
/* 278:    */     }
/* 279:355 */     return best;
/* 280:    */   }
/* 281:    */   
/* 282:    */   private HashSet<Candidate> removeDuplicates(HashSet<Candidate> set, boolean isIntermediate)
/* 283:    */   {
/* 284:360 */     HashSet<Candidate> finalSentences = new HashSet();
/* 285:361 */     if (CONFIG_TURN_ON_DUP_ELIM)
/* 286:    */     {
/* 287:381 */       List<Candidate> list = new ArrayList(set);
/* 288:383 */       for (int i = 0; i < list.size(); i++)
/* 289:    */       {
/* 290:385 */         Candidate info = (Candidate)list.get(i);
/* 291:386 */         info.discard = false;
/* 292:387 */         List<Node> nl = getNodeList(info.sent);
/* 293:388 */         info.theNodeList = nl;
/* 294:    */       }
/* 295:391 */       int startFrom = 0;
/* 296:396 */       for (int a = 0; a < list.size(); a++) {
/* 297:399 */         if (!((Candidate)list.get(a)).discard)
/* 298:    */         {
/* 299:401 */           Candidate prevSentence = (Candidate)list.get(a);
/* 300:402 */           Candidate best = (Candidate)list.get(a);
/* 301:405 */           for (int b = 0; b < list.size(); b++) {
/* 302:408 */             if ((!((Candidate)list.get(b)).discard) && (a != b))
/* 303:    */             {
/* 304:410 */               Candidate currSentence = (Candidate)list.get(b);
/* 305:411 */               double overlap = computeCandidateSimScore(currSentence, best);
/* 306:415 */               if (isIntermediate)
/* 307:    */               {
/* 308:417 */                 if (overlap > CONFIG_DUPLICATE_COLLAPSE_THRESHOLD) {
/* 309:419 */                   best = remove(currSentence, best);
/* 310:    */                 }
/* 311:    */               }
/* 312:425 */               else if (overlap > CONFIG_DUPLICATE_THRESHOLD) {
/* 313:427 */                 best = remove(currSentence, best);
/* 314:    */               }
/* 315:    */             }
/* 316:    */           }
/* 317:435 */           finalSentences.add(best);
/* 318:436 */           best.discard = true;
/* 319:    */         }
/* 320:    */       }
/* 321:    */     }
/* 322:    */     else
/* 323:    */     {
/* 324:457 */       finalSentences = set;
/* 325:    */     }
/* 326:459 */     return finalSentences;
/* 327:    */   }
/* 328:    */   
/* 329:    */   private boolean traverse(Node x, List<int[]> overlapList, String str, double pathScore, int pathLength, boolean isCollapsedCandidate, boolean overlapSame)
/* 330:    */   {
/* 331:467 */     if (!shouldContinueTraverse(x, overlapList, pathLength, pathScore)) {
/* 332:468 */       return true;
/* 333:    */     }
/* 334:472 */     if (isVEN(x, pathLength, isCollapsedCandidate)) {
/* 335:474 */       if (processVEN(x, pathLength, overlapList, isCollapsedCandidate, str, pathScore)) {
/* 336:474 */         return true;
/* 337:    */       }
/* 338:    */     }
/* 339:476 */     processNext(x, str, overlapList, pathScore, pathLength, isCollapsedCandidate);
/* 340:    */     
/* 341:478 */     return true;
/* 342:    */   }
/* 343:    */   
/* 344:    */   private boolean processVEN(Node x, int pathLength, List<int[]> theNodeList, boolean isCollapsedCandidate, String str, double pathScore)
/* 345:    */   {
/* 346:486 */     String theCandidateStr = str;
/* 347:487 */     int thePathLen = pathLength;
/* 348:488 */     double theScore = pathScore;
/* 349:492 */     if (isEndToken(x))
/* 350:    */     {
/* 351:493 */       theCandidateStr = theCandidateStr.substring(0, theCandidateStr.lastIndexOf(" "));
/* 352:494 */       thePathLen = pathLength - 1;
/* 353:    */     }
/* 354:499 */     double theAdjustedScore = computeAdjustedScore(theScore, thePathLen);
/* 355:502 */     if (isValidCandidate(this.mAnchor + " " + theCandidateStr)) {
/* 356:505 */       if (!isCollapsedCandidate)
/* 357:    */       {
/* 358:508 */         this.shortlisted.add(new Candidate(theAdjustedScore, theCandidateStr, theNodeList, thePathLen));
/* 359:    */       }
/* 360:    */       else
/* 361:    */       {
/* 362:512 */         Candidate cc = (Candidate)this.ccList.get(theCandidateStr);
/* 363:513 */         int ccPathLength = thePathLen - this.mAnchorPathLen;
/* 364:514 */         double ccPathScore = theScore - this.mAnchorPathScore;
/* 365:516 */         if (cc != null)
/* 366:    */         {
/* 367:519 */           cc.gain = Math.max(cc.gain, theAdjustedScore);
/* 368:    */         }
/* 369:    */         else
/* 370:    */         {
/* 371:523 */           cc = new Candidate(theAdjustedScore, theCandidateStr, theNodeList, ccPathLength, ccPathScore, 0.0D - this.beforeAttachGain);
/* 372:524 */           this.ccList.put(theCandidateStr, cc);
/* 373:    */         }
/* 374:526 */         return true;
/* 375:    */       }
/* 376:    */     }
/* 377:530 */     return false;
/* 378:    */   }
/* 379:    */   
/* 380:    */   public SimpleDirectedGraph<Node, DefaultWeightedEdge> getGraph()
/* 381:    */   {
/* 382:534 */     return this.mGraph;
/* 383:    */   }
/* 384:    */   
/* 385:    */   public abstract double computeAdjustedScore(double paramDouble, int paramInt);
/* 386:    */   
/* 387:    */   public abstract double computeScore(double paramDouble, List<int[]> paramList, int paramInt);
/* 388:    */   
/* 389:    */   public abstract double computeCandidateSimScore(Candidate paramCandidate1, Candidate paramCandidate2);
/* 390:    */   
/* 391:    */   public abstract boolean isEndToken(Node paramNode);
/* 392:    */   
/* 393:    */   public abstract boolean shouldContinueTraverse(Node paramNode, List<int[]> paramList, int paramInt, double paramDouble);
/* 394:    */   
/* 395:    */   public abstract boolean isValidCandidate(String paramString);
/* 396:    */   
/* 397:    */   public abstract boolean isVSN(Node paramNode);
/* 398:    */   
/* 399:    */   public abstract boolean isVEN(Node paramNode, int paramInt, boolean paramBoolean);
/* 400:    */   
/* 401:    */   public abstract List<int[]> getNodeOverlap(List<int[]> paramList1, List<int[]> paramList2);
/* 402:    */ }


