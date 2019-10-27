/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.PrintStream;
/*   5:    */ import java.io.Writer;
/*   6:    */ import java.util.ArrayList;
/*   7:    */ import java.util.Collection;
/*   8:    */ import java.util.HashMap;
/*   9:    */ import java.util.HashSet;
/*  10:    */ import java.util.Iterator;
/*  11:    */ import java.util.List;
/*  12:    */ import java.util.Set;
/*  13:    */ import org.jgrapht.graph.DefaultWeightedEdge;
/*  14:    */ import org.jgrapht.graph.SimpleDirectedGraph;
/*  15:    */ import org.jgrapht.graph.SimpleDirectedWeightedGraph;
/*  16:    */ import org.textbug.utility.MathUtil;
/*  17:    */ 
/*  18:    */ public class OpinosisSummarizer
/*  19:    */   extends OpinosisSettings
/*  20:    */   implements OpinosisFramework
/*  21:    */ {
/*  22:    */   protected SimpleDirectedGraph<Node, DefaultWeightedEdge> mGraph2;
/*  23:    */   protected static final boolean DEBUG = false;
/*  24: 29 */   static int clusterID = 0;
/*  25: 31 */   static int oldClusterID = -1;
/*  26: 34 */   String beforeAttach = "";
/*  27: 35 */   double beforeAttachGain = 0.0D;
/*  28: 36 */   double beforeAttachScore = 0.0D;
/*  29: 38 */   private int originalLevel = 0;
/*  30:    */   Writer writer;
/*  31: 57 */   HashSet<SentenceInfo> shortlisted = new HashSet();
/*  32: 61 */   HashMap<String, SentenceInfo> tempCollapsed = new HashMap();
/*  33: 71 */   HashMap<String, Node> wordNodeMap = null;
/*  34:    */   
/*  35:    */   class Topic
/*  36:    */     implements Comparable<Topic>
/*  37:    */   {
/*  38: 74 */     
/*  39: 76 */     List<int[]> sids = null;
/*  40:    */     
/*  41:    */     public Topic(List<int[]> str)
/*  42:    */     {
/*  43: 78 */       
/*  44: 79 */       this.sids = str;
/*  45:    */     }
/*  46:    */     
/*  47:    */     public int compareTo(Topic t)
/*  48:    */     {
/*  49: 84 */       if (t.sids.size() > this.sids.size()) {
/*  50: 85 */         return 1;
/*  51:    */       }
/*  52: 88 */       if (t.sids.size() < this.sids.size()) {
/*  53: 89 */         return -1;
/*  54:    */       }
/*  55: 91 */       return 0;
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   public OpinosisSummarizer(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer)
/*  60:    */   {
/*  61:101 */     this.mGraph2 = g;
/*  62:102 */     this.writer = printer;
/*  63:103 */     this.wordNodeMap = wordNodeMap;
/*  64:    */   }
/*  65:    */   
/*  66:    */   private double computeAdjustedScore(boolean concatOn, int level, double pathProb, int intersect)
/*  67:    */   {
/*  68:109 */     double ixt = intersect;
/*  69:111 */     if (intersect > 0) {
/*  70:112 */       ixt = Math.log(ixt);
/*  71:    */     }
/*  72:115 */     return ixt + pathProb;
/*  73:    */   }
/*  74:    */   
/*  75:    */   private boolean doCheck(int level, boolean concatOn)
/*  76:    */   {
/*  77:124 */     boolean valid = false;
/*  78:125 */     if (level >= this.P_MIN_SENT_LENGTH)
/*  79:    */     {
/*  80:127 */       if (concatOn)
/*  81:    */       {
/*  82:128 */         if (level - this.originalLevel > 0) {
/*  83:129 */           valid = true;
/*  84:    */         }
/*  85:    */       }
/*  86:    */       else {
/*  87:133 */         valid = true;
/*  88:    */       }
/*  89:    */     }
/*  90:    */     else {
/*  91:136 */       valid = false;
/*  92:    */     }
/*  93:139 */     return valid;
/*  94:    */   }
/*  95:    */   
/*  96:    */   private boolean doCollapse(Node x, List<int[]> YintersectX, double pathscore, double prevPathScore, String str, List<int[]> overlapList, int level, boolean concatOn)
/*  97:    */   {
/*  98:147 */     this.beforeAttach = str;
/*  99:148 */     this.beforeAttachScore = prevPathScore;
/* 100:149 */     this.beforeAttachGain = 0.0D;
/* 101:150 */     this.originalLevel = level;
/* 102:    */     
/* 103:152 */     Set<DefaultWeightedEdge> edges = this.mGraph2.outgoingEdgesOf(x);
/* 104:153 */     if ((edges != null) && (edges.size() > 1))
/* 105:    */     {
/* 106:155 */       Iterator<DefaultWeightedEdge> cIter = edges.iterator();
/* 107:156 */       while (cIter.hasNext())
/* 108:    */       {
/* 109:158 */         DefaultWeightedEdge cEdgeOfX = (DefaultWeightedEdge)cIter.next();
/* 110:159 */         Node cY = (Node)this.mGraph2.getEdgeTarget(cEdgeOfX);
/* 111:160 */         String cYNodeName = cY.getNodeName();
/* 112:    */         
/* 113:    */ 
/* 114:    */ 
/* 115:    */ 
/* 116:165 */         List<int[]> cYintersectX = cY.getSetenceOverlapRight(overlapList);
/* 117:    */         
/* 118:    */ 
/* 119:168 */         int newLevel = level + 1;
/* 120:169 */         double newPathScore = getCurrentGain(pathscore, cYintersectX.size(), newLevel);
/* 121:172 */         if (cYintersectX.size() >= CONFIG_MIN_REDUNDANCY) {
/* 122:173 */           traverse(cY, cYintersectX, "xx " + cYNodeName, newPathScore, newLevel, true, false);
/* 123:    */         }
/* 124:    */       }
/* 125:    */     }
/* 126:176 */     concatOn = false;
/* 127:    */     
/* 128:    */ 
/* 129:    */ 
/* 130:    */ 
/* 131:181 */     return processFound();
/* 132:    */   }
/* 133:    */   
/* 134:    */   public void doSummary()
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:192 */     long startTime = 0L;
/* 138:193 */     long endTime = 0L;
/* 139:    */     
/* 140:    */ 
/* 141:    */ 
/* 142:    */ 
/* 143:198 */     Set<Node> vertexSet = this.mGraph2.vertexSet();
/* 144:    */     
/* 145:    */ 
/* 146:201 */     Iterator<Node> vertexSetIterator = vertexSet.iterator();
/* 147:202 */     while (vertexSetIterator.hasNext())
/* 148:    */     {
/* 149:205 */       Node x = (Node)vertexSetIterator.next();
/* 150:207 */       if (isVSN(x))
/* 151:    */       {
/* 152:212 */         double score = 0.0D;
/* 153:213 */         traverse(x, x.getSentenceIds(), x.getNodeName(), score, 1, false, false);
/* 154:    */       }
/* 155:    */     }
/* 156:220 */     List<SentenceInfo> theSentenceInfos = getFinalSentences();
/* 157:227 */     for (SentenceInfo info : theSentenceInfos)
/* 158:    */     {
/* 159:228 */       info.sent = info.sent.replaceAll("(/[a-z,.;$]+(\\s+|$))", " ");
/* 160:229 */       info.sent = info.sent.replaceAll("xx", "");
/* 161:230 */       info.sent += " .";
/* 162:231 */       info.sent = info.sent.replaceAll("\\s+", " ");
/* 163:232 */       this.writer.append(info.sent);
/* 164:    */       
/* 165:    */ 
/* 166:    */ 
/* 167:    */ 
/* 168:    */ 
/* 169:    */ 
/* 170:    */ 
/* 171:240 */       this.writer.append("\n");
/* 172:    */     }
/* 173:243 */     this.writer.close();
/* 174:    */   }
/* 175:    */   
/* 176:    */   public boolean isVSN(Node x)
/* 177:    */   {
/* 178:249 */     String nname = x.getNodeName();
/* 179:251 */     if (x.getAveragePos() <= 15.0D) {
/*  85:165 */       if ((nname.contains("/adj")) || 
/*  86:166 */         (nname.contains("/adv")) || 
/*  87:167 */         (nname.contains("/propess$")) || 
/*  89:169 */         (nname.contains("/n")) || 
/*  90:170 */         (nname.contains("/art")) || 
/*  91:171 */         (nname.matches("^(seu/|sua/|quando/|um/|uma/|o/|a/|os/|as/|ele/|ela/|eles/|elas/|esse/|essa|eu/|nós/|nosso/|nossa/).*")) || 
/*  92:172 */         (nname.contains("o/propess")) || 
/*  93:173 */         (nname.contains("se/")) || 
/*  94:174 */         (nname.contains("para/"))) {
/*  95:175 */         return true;
/* 191:    */       }
/* 192:    */     }
/* 193:264 */     return false;
/* 194:    */   }
/* 195:    */   
/* 196:    */   private double getCurrentGain(double gain, double r, int level)
/* 197:    */   {
/* 198:269 */     double theGain = 0.0D;
/* 199:271 */     if (CONFIG_SCORING_FUNCTION == GAIN_REDUNDANCY_ONLY) {
/* 200:272 */       theGain = gain + r;
/* 201:    */     }
/* 202:275 */     if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LEVEL) {
/* 203:276 */       theGain = gain + r * level;
/* 204:    */     }
/* 205:279 */     if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL) {
/* 206:280 */       if (level > 1) {
/* 207:281 */         theGain = gain + r * MathUtil.getLog2(level);
/* 208:    */       } else {
/* 209:284 */         theGain = gain + r;
/* 210:    */       }
/* 211:    */     }
/* 212:287 */     return theGain;
/* 213:    */   }
/* 214:    */   
/* 215:    */   private List<SentenceInfo> getFinalSentences()
/* 216:    */   {
/* 217:305 */     throw new Error("Unresolved compilation problem: \n\tThe method sort(List<T>, Comparator<? super T>) in the type Collections is not applicable for the arguments (List<SentenceInfo>, SummarySorter)\n");
/* 218:    */   }
/* 219:    */   
/* 220:    */   private double getGreedyWeight(Node y)
/* 221:    */   {
/* 222:337 */     double theScore = 1.0D;
/* 223:339 */     if ((y.getNodeName().contains("/n")) || (y.getNodeName().matches(".*/v[dng]"))) {
/* 224:340 */       theScore = 2.0D;
/* 225:343 */     } else if (y.getNodeName().matches("(este|estes|esta|estas|esse|esses|essa|essas|o|a|os|as)/art")) {
/* 226:344 */       theScore = 1.5D;
/* 227:347 */     } else if (y.getNodeName().matches("(eu|ele|ela|nós|nosso)/propess")) {
/* 228:348 */       theScore = 1.5D;
/* 229:351 */     } else if (y.getNodeName().contains("/adj")) {
/* 230:352 */       theScore = 1.8D;
/* 231:355 */     } else if ((y.getNodeName().contains("não/adv")) || (y.getNodeName().contains("ont/")) || (y.getNodeName().contains("cant/")) || (y.getNodeName().contains("nt/adv"))) {
/* 232:356 */       theScore = 2.0D;
/* 233:359 */     } else if ((y.getNodeName().contains("/?")) || (y.getNodeName().contains("/!"))) {
/* 234:360 */       theScore = 0.5D;
/* 235:363 */     } else if (y.getNodeName().contains("/.")) {
/* 236:364 */       theScore = 1.5D;
/* 237:372 */     } else if ((y.getNodeName().contains("/art")) || (y.getNodeName().contains("/wdt")) || (y.getNodeName().contains("/adv")) || (y.getNodeName().contains("/ks")) || (y.getNodeName().contains("/kc")) || (y.getNodeName().contains("/num"))) {
/* 238:374 */       theScore = 0.1D;
/* 239:    */     }
/* 240:377 */     return theScore;
/* 241:    */   }
/* 242:    */   
/* 243:    */   private List<Node> getNodeList(String sent)
/* 244:    */   {
/* 245:385 */     String[] tokens = sent.split("\\s+");
/* 246:386 */     ArrayList<Node> l = new ArrayList();
/* 247:388 */     for (String token : tokens) {
/* 248:390 */       if (token.matches(".*(/n|/adj|/v[a-s]).*"))
/* 249:    */       {
/* 250:392 */         Node n = (Node)this.wordNodeMap.get(token);
/* 251:394 */         if (n != null) {
/* 252:395 */           l.add(n);
/* 253:    */         }
/* 254:    */       }
/* 255:    */     }
/* 256:401 */     return l;
/* 257:    */   }
/* 258:    */   
/* 259:    */   private double getOverallGain(double score, int level)
/* 260:    */   {
/* 261:411 */     double oGain = score;
/* 262:413 */     if (CONFIG_NORMALIZE_OVERALLGAIN) {
/* 263:414 */       oGain /= level;
/* 264:    */     }
/* 265:425 */     return oGain;
/* 266:    */   }
/* 267:    */   
/* 268:    */   private double getPathProb(Node x, Node y, double score, List l)
/* 269:    */   {
/* 270:429 */     double pathProb = y.getSetencePathProb2(x.getNodeProb(), x.getSentenceIds(), 1.0D);
/* 271:430 */     double currOverlap = l.size();
/* 272:    */     
/* 273:432 */     pathProb = currOverlap + score;
/* 274:    */     
/* 275:434 */     return pathProb;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public double getSentenceJaccardOverlap(SentenceInfo s1, SentenceInfo s2)
/* 279:    */   {
/* 280:438 */     List<Node> l1 = s1.theNodeList;
/* 281:439 */     List<Node> l2 = s2.theNodeList;
/* 282:    */     
/* 283:441 */     HashSet union = new HashSet(l1);
/* 284:442 */     HashSet intersect = new HashSet(l1);
/* 285:    */     
/* 286:444 */     union.addAll(l2);
/* 287:445 */     intersect.retainAll(l2);
/* 288:    */     
/* 289:447 */     double overlap = intersect.size() / union.size();
/* 290:    */     
/* 291:    */ 
/* 292:450 */     return overlap;
/* 293:    */   }
/* 294:    */   
/* 295:    */   private boolean isValidEnd(Node x, int level, boolean concatOn)
/* 296:    */   {
/* 297:473 */     boolean valid = false;
/* 298:476 */     if (matchesEndPattern(x.getNodeName())) {
/* 299:477 */       return doCheck(level - 1, concatOn);
/* 300:    */     }
/* 301:481 */     if (this.mGraph2.outDegreeOf(x) <= 0) {
/* 302:482 */       return doCheck(level, concatOn);
/* 303:    */     }
/* 304:486 */     return valid;
/* 305:    */   }
/* 306:    */   
/* 307:    */   private boolean isValidSentence(Node x, List<int[]> overlapList, int level, double totalgain, String str)
/* 308:    */   {
/* 309:492 */     boolean isGood = false;
System.out.println(str);
/* 310:494 */     if (str.matches(".*(/adj)*.*(/n)+.*(/v)+.*(/adj)+.*")) {
/* 311:495 */       isGood = true;
/* 312:498 */     }else if ((!str.matches(".*(/art).*")) && (str.matches(".*(/adv)*.*(/adj)+.*(/n)+.*"))) {
/* 313:499 */       isGood = true;
/* 314:502 */     } else if (str.matches(".*(/propess|/art)+.*(/v)+.*(/adv|/adj)+.*(/n)+.*")) {
/* 315:503 */       isGood = true;
/* 316:506 */     //} else if (str.matches(".*(/adj)+.*(/to)+.*(/vb).*")) {
/* 317:507 */       //isGood = true;
/* 318:510 */     } else if (str.matches(".*(/adv)+.*(/ks|/prep)+.*(/n)+.*")) {
/* 319:511 */       isGood = true;
/* 320:    */     }
/* 321:542 */     String last = str.substring(str.lastIndexOf(' '), str.length());
/* 322:543 */     if (last.matches(".*(/ks|/prep|/kc|/propess|/art|/,)")) {
/* 323:544 */       isGood = false;
/* 324:    */     }
/* 325:547 */     return isGood;
/* 326:    */   }
/* 327:    */   
/* 328:    */   private boolean matchesEndPattern(String str)
/* 329:    */   {
/* 330:559 */     if (str.matches(".*(/\\.|/,)")) {
/* 331:560 */       return true;
/* 332:    */     }
/* 333:564 */     return false;
/* 334:    */   }
/* 335:    */   
/* 336:    */   private void print()
/* 337:    */   {
/* 338:568 */     System.out.print("");
/* 339:    */   }
/* 340:    */   
/* 341:    */   private void print(String str)
/* 342:    */   {
/* 343:573 */     System.out.print(str + " ");
/* 344:    */   }
/* 345:    */   
/* 346:    */   private void println(String str)
/* 347:    */   {
/* 348:577 */     System.out.println(str + " ");
/* 349:    */   }
/* 350:    */   
/* 351:    */   private boolean processFound()
/* 352:    */   {
/* 353:581 */     boolean success = false;
/* 354:582 */     Collection<SentenceInfo> temp = this.tempCollapsed.values();
/* 355:583 */     HashSet<SentenceInfo> collapsed = new HashSet(temp);
/* 356:    */     
/* 357:585 */     collapsed = removeDuplicates(collapsed, true);
/* 358:    */     
/* 359:587 */     int i = 0;
/* 360:588 */     if (collapsed.size() > 1)
/* 361:    */     {
/* 362:590 */       double overallgains = 0.0D;
/* 363:591 */       double allscores = this.beforeAttachScore;
/* 364:592 */       double allgains = this.beforeAttachGain;
/* 365:593 */       int alllevels = this.originalLevel;
/* 366:    */       
/* 367:595 */       StringBuffer buffer = new StringBuffer(this.beforeAttach);
/* 368:596 */       List<int[]> sentList = new ArrayList();
/* 369:598 */       for (SentenceInfo theInfo : collapsed)
/* 370:    */       {
/* 371:603 */         overallgains += theInfo.gain;
/* 372:604 */         allgains += theInfo.localgain;
/* 373:605 */         allscores += theInfo.rawscore;
/* 374:606 */         alllevels += theInfo.level;
/* 375:607 */         sentList.addAll(theInfo.sentList);
/* 376:608 */         if ((i > 0) && (i == collapsed.size() - 1)) {
/* 377:609 */           buffer.append(" e ");
/* 378:611 */         } else if (i > 0) {
/* 379:612 */           buffer.append(" , ");
/* 380:    */         } else {
/* 381:615 */           buffer.append(" ");
/* 382:    */         }
/* 383:617 */         buffer.append(theInfo.sent);
/* 384:618 */         i++;
/* 385:    */       }
/* 386:623 */       if (this.beforeAttach.contains("produto foi")) {
/* 387:624 */         print();
/* 388:    */       }
/* 389:626 */       if (this.tempCollapsed.size() > 1)
/* 390:    */       {
/* 391:628 */         double overallGain = overallgains / this.tempCollapsed.size();
/* 392:    */         
/* 393:    */ 
/* 394:631 */         this.shortlisted.add(new SentenceInfo(overallGain, buffer.toString(), sentList, alllevels));
/* 395:    */         
/* 396:633 */         success = true;
/* 397:    */       }
/* 398:    */     }
/* 399:640 */     this.tempCollapsed.clear();
/* 400:641 */     this.beforeAttach = "";
/* 401:642 */     this.beforeAttachGain = 0.0D;
/* 402:643 */     this.beforeAttachScore = 0.0D;
/* 403:644 */     this.originalLevel = 0;
/* 404:    */     
/* 405:646 */     return success;
/* 406:    */   }
/* 407:    */   
/* 408:    */   private void processNext(Node x, String str, Set<DefaultWeightedEdge> outgoing, List<int[]> overlapList, double pathscore, int level, boolean concatOn)
/* 409:    */   {
/* 410:650 */     Iterator<DefaultWeightedEdge> iter = outgoing.iterator();
/* 411:651 */     boolean doMore = true;
/* 412:652 */     while ((iter.hasNext()) && (doMore))
/* 413:    */     {
/* 414:654 */       DefaultWeightedEdge edgeOfX = (DefaultWeightedEdge)iter.next();
/* 415:    */       
/* 416:656 */       Node y = (Node)this.mGraph2.getEdgeTarget(edgeOfX);
/* 417:657 */       String yNodeName = y.getNodeName();
/* 418:658 */       List<int[]> YintersectX = y.getSetenceOverlapRight(overlapList);
/* 419:    */       
/* 420:    */ 
/* 421:661 */       double wt = getGreedyWeight(y);
/* 422:665 */       if (YintersectX.size() > 0)
/* 423:    */       {
/* 424:666 */         int newLevel = level + 1;
/* 425:667 */         double newPathScore = getCurrentGain(pathscore, YintersectX.size(), newLevel);
/* 426:670 */         if ((CONFIG_TURN_ON_COLLAPSE) && (level >= CONFIG_ATTACHMENT_AFTER) && (!concatOn) && (YintersectX.size() <= overlapList.size()) && (x.getNodeName().matches(".*(/v[a-z]|/ks)")))
/* 427:    */         {
/* 428:673 */           boolean success = doCollapse(x, YintersectX, newPathScore, pathscore, str, overlapList, level, concatOn);
/* 429:675 */           if (!success)
/* 430:    */           {
/* 431:677 */             String strTemp = str + " " + y.getNodeName();
/* 432:678 */             doMore = traverse(y, YintersectX, strTemp, newPathScore, newLevel, concatOn, false);
/* 433:    */           }
/* 434:    */         }
/* 435:    */         else
/* 436:    */         {
/* 437:685 */           String strTemp = str + " " + yNodeName;
/* 438:686 */           doMore = traverse(y, YintersectX, strTemp, newPathScore, level + 1, concatOn, false);
/* 439:    */         }
/* 440:    */       }
/* 441:    */     }
/* 442:    */   }
/* 443:    */   
/* 444:    */   private SentenceInfo remove(SentenceInfo currSentence, SentenceInfo best)
/* 445:    */   {
/* 446:693 */     double temp = currSentence.gain;
/* 447:695 */     if ((best.gain < currSentence.gain) && (best.level <= currSentence.level))
/* 448:    */     {
/* 449:697 */       best.discard = true;
/* 450:698 */       best = currSentence;
/* 451:    */     }
/* 452:    */     else
/* 453:    */     {
/* 454:702 */       currSentence.discard = true;
/* 455:    */     }
/* 456:705 */     return best;
/* 457:    */   }
/* 458:    */   
/* 459:    */   private HashSet<SentenceInfo> removeDuplicates(HashSet<SentenceInfo> set, boolean isIntermediate)
/* 460:    */   {
/* 461:710 */     HashSet<SentenceInfo> finalSentences = new HashSet();
/* 462:711 */     if (CONFIG_TURN_ON_DUP_ELIM)
/* 463:    */     {
/* 464:731 */       List<SentenceInfo> list = new ArrayList(set);
/* 465:733 */       for (int i = 0; i < list.size(); i++)
/* 466:    */       {
/* 467:735 */         SentenceInfo info = (SentenceInfo)list.get(i);
/* 468:736 */         info.discard = false;
/* 469:737 */         List<Node> nl = getNodeList(info.sent);
/* 470:738 */         info.theNodeList = nl;
/* 471:    */       }
/* 472:749 */       int startFrom = 0;
/* 473:754 */       for (int a = 0; a < list.size(); a++) {
/* 474:757 */         if (!((SentenceInfo)list.get(a)).discard)
/* 475:    */         {
/* 476:759 */           SentenceInfo prevSentence = (SentenceInfo)list.get(a);
/* 477:760 */           SentenceInfo best = (SentenceInfo)list.get(a);
/* 478:763 */           for (int b = 0; b < list.size(); b++) {
/* 479:766 */             if ((!((SentenceInfo)list.get(b)).discard) && (a != b))
/* 480:    */             {
/* 481:768 */               SentenceInfo currSentence = (SentenceInfo)list.get(b);
/* 482:769 */               double overlap = getSentenceJaccardOverlap(currSentence, best);
/* 483:773 */               if (isIntermediate)
/* 484:    */               {
/* 485:775 */                 if (overlap > CONFIG_DUPLICATE_COLLAPSE_THRESHOLD) {
/* 486:777 */                   best = remove(currSentence, best);
/* 487:    */                 }
/* 488:    */               }
/* 489:783 */               else if (overlap > CONFIG_DUPLICATE_THRESHOLD) {
/* 490:785 */                 best = remove(currSentence, best);
/* 491:    */               }
/* 492:    */             }
/* 493:    */           }
/* 494:793 */           finalSentences.add(best);
/* 495:794 */           best.discard = true;
/* 496:    */         }
/* 497:    */       }
/* 498:    */     }
/* 499:    */     else
/* 500:    */     {
/* 501:815 */       finalSentences = set;
/* 502:    */     }
/* 503:817 */     return finalSentences;
/* 504:    */   }
/* 505:    */   
/* 506:    */   private SentenceInfo removeFinal(SentenceInfo currSentence, SentenceInfo best)
/* 507:    */   {
/* 508:821 */     double temp = currSentence.gain;
/* 509:823 */     if ((best.gain < currSentence.gain) && (best.level <= currSentence.level))
/* 510:    */     {
/* 511:825 */       best.discard = true;
/* 512:826 */       best = currSentence;
/* 513:    */     }
/* 514:    */     else
/* 515:    */     {
/* 516:830 */       currSentence.discard = true;
/* 517:    */     }
/* 518:837 */     return best;
/* 519:    */   }
/* 520:    */   
/* 521:    */   private boolean shouldContinue(Node x, List<int[]> overlapList, int level, double score, String str)
/* 522:    */   {
/* 523:850 */     if (level >= this.P_MAX_SENT_LENGTH) {
/* 524:851 */       return false;
/* 525:    */     }
/* 526:854 */     if (score == (-1.0D / 0.0D)) {
/* 527:855 */       return false;
/* 528:    */     }
/* 529:858 */     if ((overlapList.size() < CONFIG_MIN_REDUNDANCY) && (!matchesEndPattern(x.getNodeName()))) {
/* 530:860 */       return false;
/* 531:    */     }
/* 532:869 */     return true;
/* 533:    */   }
/* 534:    */   
/* 535:    */   private boolean traverse(Node x, List<int[]> overlapList, String str, double pathscore, int level, boolean concatOn, boolean overlapSame)
/* 536:    */   {
/* 537:881 */     if (!shouldContinue(x, overlapList, level, pathscore, str)) {
/* 538:884 */       return true;
/* 539:    */     }
/* 540:888 */     if (isValidEnd(x, level, concatOn))
/* 541:    */     {
/* 542:889 */       String tempStr = str;
/* 543:890 */       int tempLevel = level;
/* 544:891 */       double tempScore = pathscore;
/* 545:893 */       if (matchesEndPattern(x.getNodeName()))
/* 546:    */       {
/* 547:894 */         tempStr = tempStr.substring(0, tempStr.lastIndexOf(" "));
/* 548:895 */         tempLevel = level - 1;
/* 549:    */       }
/* 550:899 */       double overallGain = getOverallGain(tempScore, tempLevel);
/* 551:904 */       if (isValidSentence(x, overlapList, tempLevel, overallGain, 
/* 552:905 */         this.beforeAttach + " " + tempStr)) {
/* 553:908 */         if (!concatOn)
/* 554:    */         {
/* 555:909 */           this.shortlisted.add(new SentenceInfo(overallGain, tempStr, overlapList, tempLevel));
/* 556:    */         }
/* 557:    */         else
/* 558:    */         {
/* 559:912 */           SentenceInfo sinfo = (SentenceInfo)this.tempCollapsed.get(tempStr);
/* 560:913 */           if (sinfo != null)
/* 561:    */           {
/* 562:914 */             sinfo.gain = Math.max(sinfo.gain, overallGain);
/* 563:    */           }
/* 564:    */           else
/* 565:    */           {
/* 566:916 */             sinfo = new SentenceInfo(overallGain, tempStr, overlapList, tempLevel - this.originalLevel, pathscore - this.beforeAttachScore, 0.0D - this.beforeAttachGain);
/* 567:917 */             this.tempCollapsed.put(tempStr, sinfo);
/* 568:    */           }
/* 569:919 */           return true;
/* 570:    */         }
/* 571:    */       }
/* 572:    */     }
/* 573:925 */     Set<DefaultWeightedEdge> outgoing = this.mGraph2.outgoingEdgesOf(x);
/* 574:926 */     if ((outgoing != null) && 
/* 575:927 */       (outgoing.size() > 0)) {
/* 576:928 */       processNext(x, str, outgoing, overlapList, pathscore, 
/* 577:929 */         level, concatOn);
/* 578:    */     }
/* 579:933 */     return true;
/* 580:    */   }
/* 581:    */   
/* 582:    */   public boolean isVEN(Node x)
/* 583:    */   {
/* 584:940 */     return false;
/* 585:    */   }
/* 586:    */   
/* 587:    */   public boolean isValidPath(Node x)
/* 588:    */   {
/* 589:946 */     return false;
/* 590:    */   }
/* 591:    */ }


