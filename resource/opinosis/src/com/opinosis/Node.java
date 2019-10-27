/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import java.util.ArrayList;
/*   4:    */ import java.util.HashSet;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public class Node
/*   8:    */ {
/*   9: 18 */   private List<Integer> docIds = new ArrayList();
/*  10: 21 */   private String nodeName = "-1";
/*  11: 22 */   private List<int[]> sentenceIds = new ArrayList();
/*  12: 23 */   static HashSet<Integer> sentences = new HashSet();
/*  13:    */   
/*  14:    */   public int getTotalSentences()
/*  15:    */   {
/*  16: 29 */     return sentences.size();
/*  17:    */   }
/*  18:    */   
/*  19: 31 */   private static int TOTAL_TOKENS = 0;
/*  20: 32 */   private int nodeCount = 0;
/*  21: 34 */   private int minPos = -1;
/*  22: 36 */   private int avgPos = 0;
/*  23:    */   
/*  24:    */   public List<Integer> getDocIds()
/*  25:    */   {
/*  26: 42 */     return this.docIds;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public List<int[]> getSentenceIds()
/*  30:    */   {
/*  31: 49 */     return this.sentenceIds;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public String getNodeName()
/*  35:    */   {
/*  36: 56 */     return this.nodeName;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void addDocId(int docId)
/*  40:    */   {
/*  41: 63 */     this.docIds.add(Integer.valueOf(docId));
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void addSentenceId(int sentenceId, int pos)
/*  45:    */   {
/*  46: 70 */     TOTAL_TOKENS += 1;
/*  47: 71 */     this.nodeCount += 1;
/*  48: 72 */     int[] k = new int[2];
/*  49: 73 */     k[0] = sentenceId;
/*  50: 74 */     k[1] = pos;
/*  51:    */     
/*  52: 76 */     sentences.add(Integer.valueOf(sentenceId));
/*  53: 77 */     this.sentenceIds.add(k);
/*  54: 80 */     if (pos < this.minPos) {
/*  55: 81 */       this.minPos = pos;
/*  56:    */     }
/*  57: 83 */     this.avgPos += pos;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public double getMinPos()
/*  61:    */   {
/*  62: 87 */     return this.minPos;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public double getAveragePos()
/*  66:    */   {
/*  67: 91 */     return this.avgPos / this.sentenceIds.size();
/*  68:    */   }
/*  69:    */   
/*  70:    */   public double getNodeCount()
/*  71:    */   {
/*  72: 95 */     return this.nodeCount + 0.01D;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public static double getTotalNodeCount()
/*  76:    */   {
/*  77: 99 */     return TOTAL_TOKENS + 0.01D;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public void setNodeName(String nodeName)
/*  81:    */   {
/*  82:106 */     this.nodeName = nodeName;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public List<Integer> getSetenceOverlap(Node n1, Node n2)
/*  86:    */   {
/*  87:111 */     List<Integer> l1 = n1.getDocIds();
/*  88:112 */     List<Integer> l2 = n2.getDocIds();
/*  89:    */     
/*  90:114 */     List<Integer> l3 = new ArrayList();
/*  91:    */     
/*  92:116 */     l3.addAll(l1);
/*  93:117 */     l3.retainAll(l2);
/*  94:    */     
/*  95:119 */     return l3;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public List<int[]> getSetenceOverlapRight(List<int[]> left)
/*  99:    */   {
/* 100:124 */     List<int[]> right = getSentenceIds();
/* 101:125 */     List<int[]> l3 = new ArrayList();
/* 102:    */     
/* 103:127 */     int pointer = 0;
/* 104:129 */     for (int i = 0; i < left.size(); i++)
/* 105:    */     {
/* 106:130 */       int[] eleft = (int[])left.get(i);
/* 107:132 */       if (pointer > right.size()) {
/* 108:    */         break;
/* 109:    */       }
/* 110:136 */       for (int j = pointer; j < right.size(); j++)
/* 111:    */       {
/* 112:138 */         int[] eright = (int[])right.get(j);
/* 113:140 */         if (eright[0] == eleft[0])
/* 114:    */         {
/* 115:144 */           if ((eright[1] > eleft[1]) && (Math.abs(eright[1] - eleft[1]) <= OpinosisSettings.CONFIG_PERMISSABLE_GAP))
/* 116:    */           {
/* 117:146 */             l3.add(eright);
/* 118:147 */             pointer = j + 1;
/* 119:148 */             break;
/* 120:    */           }
/* 121:151 */           //eright[1];eleft[1];
/* 122:    */         }
/* 123:    */         else
/* 124:    */         {
/* 125:158 */           if (eright[0] > eleft[0]) {
/* 126:    */             break;
/* 127:    */           }
/* 128:    */         }
/* 129:    */       }
/* 130:    */     }
/* 131:165 */     return l3;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public List<int[]> getSetenceOverlap(List<int[]> l1)
/* 135:    */   {
/* 136:170 */     List<int[]> l2 = getSentenceIds();
/* 137:171 */     List<int[]> l3 = new ArrayList();
/* 138:    */     
/* 139:173 */     int pointer = 0;
/* 140:175 */     for (int i = 0; i < l1.size(); i++)
/* 141:    */     {
/* 142:176 */       int[] elem1 = (int[])l1.get(i);
/* 143:178 */       if (pointer > l2.size()) {
/* 144:    */         break;
/* 145:    */       }
/* 146:182 */       for (int j = pointer; j < l2.size(); j++)
/* 147:    */       {
/* 148:184 */         int[] elem2 = (int[])l2.get(j);
/* 149:186 */         if (elem2[0] == elem1[0])
/* 150:    */         {
/* 151:188 */           l3.add(elem2);
/* 152:189 */           pointer = j + 1;
/* 153:    */         }
/* 154:    */         else
/* 155:    */         {
/* 156:193 */           if (elem2[0] > elem1[0]) {
/* 157:    */             break;
/* 158:    */           }
/* 159:    */         }
/* 160:    */       }
/* 161:    */     }
/* 162:200 */     return l3;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public static double getSetenceJaccardOverlap(List<int[]> l1, List<int[]> l2)
/* 166:    */   {
/* 167:205 */     int last = 0;
/* 168:206 */     int intersect = 0;
/* 169:207 */     HashSet<Integer> union = new HashSet();
/* 170:208 */     for (int i = 0; i < l1.size(); i++)
/* 171:    */     {
/* 172:210 */       int elem1 = ((int[])l1.get(i))[0];
/* 173:211 */       union.add(Integer.valueOf(elem1));
/* 174:213 */       for (int j = last; j < l2.size(); j++)
/* 175:    */       {
/* 176:214 */         int elem2 = ((int[])l2.get(j))[0];
/* 177:    */         
/* 178:216 */         union.add(Integer.valueOf(elem2));
/* 179:218 */         if (elem2 == elem1)
/* 180:    */         {
/* 181:219 */           intersect++;
/* 182:220 */           last = j + 1;
/* 183:    */         }
/* 184:    */         else
/* 185:    */         {
/* 186:224 */           if (elem2 > elem1) {
/* 187:    */             break;
/* 188:    */           }
/* 189:    */         }
/* 190:    */       }
/* 191:    */     }
/* 192:235 */     double overlap = intersect / union.size();
/* 193:    */     
/* 194:237 */     return overlap;
/* 195:    */   }
/* 196:    */   
/* 197:    */   public double getSetencePathProb(double prevProb, List<int[]> l1)
/* 198:    */   {
/* 199:242 */     List l2 = getSetenceOverlapRight(l1);
/* 200:243 */     List l3 = getSetenceUnion(l1);
/* 201:    */     
/* 202:    */ 
/* 203:    */ 
/* 204:247 */     double currProb = l2.size() / l3.size();
/* 205:    */     
/* 206:249 */     return currProb;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public double getSetencePathProb2(double prevProb, List<int[]> l1, double prior)
/* 210:    */   {
/* 211:254 */     List l2 = getSetenceOverlapRight(l1);
/* 212:    */     
/* 213:256 */     double currProb = (l2.size() + 0.01D) / (TOTAL_TOKENS + 0.01D);
/* 214:    */     
/* 215:    */ 
/* 216:    */ 
/* 217:260 */     double prob = currProb / prevProb * prior;
/* 218:    */     
/* 219:262 */     return prob;
/* 220:    */   }
/* 221:    */   
/* 222:    */   private List getSetenceUnion(List<int[]> l1)
/* 223:    */   {
/* 224:267 */     List<int[]> l2 = getSentenceIds();
/* 225:    */     List<int[]> longer;
/* 226:    */     List<int[]> shorter;
/* 227:    */     //List<int[]> longer;
/* 228:272 */     if (l1.size() < l2.size())
/* 229:    */     {
/* 230:273 */       shorter = l1;
/* 231:274 */       longer = l2;
/* 232:    */     }
/* 233:    */     else
/* 234:    */     {
/* 235:276 */       shorter = l2;
/* 236:277 */       longer = l1;
/* 237:    */     }
/* 238:280 */     List<int[]> l3 = new ArrayList();
/* 239:    */     
/* 240:    */ 
/* 241:    */ 
/* 242:284 */     int pointer = 0;
/* 243:286 */     for (int i = 0; i < longer.size(); i++)
/* 244:    */     {
/* 245:288 */       int[] elem1 = (int[])longer.get(i);
/* 246:290 */       if (pointer >= shorter.size()) {
/* 247:291 */         l3.add(elem1);
/* 248:    */       }
/* 249:294 */       for (int j = pointer; j < shorter.size(); j++)
/* 250:    */       {
/* 251:297 */         int[] elem2 = (int[])shorter.get(j);
/* 252:300 */         if (elem2[0] == elem1[0])
/* 253:    */         {
/* 254:303 */           l3.add(elem2);
/* 255:304 */           pointer = j + 1;
/* 256:305 */           break;
/* 257:    */         }
/* 258:309 */         if (elem2[0] > elem1[0])
/* 259:    */         {
/* 260:311 */           l3.add(elem1);
/* 261:312 */           break;
/* 262:    */         }
/* 263:315 */         l3.add(elem2);
/* 264:316 */         pointer = j + 1;
/* 265:    */       }
/* 266:    */     }
/* 267:324 */     return l3;
/* 268:    */   }
/* 269:    */   
/* 270:    */   private List getSetenceUnion(List<int[]> l1, List<int[]> l2)
/* 271:    */   {
/* 272:    */     List<int[]> longer;
/* 273:    */     List<int[]> shorter;
/* 274:    */    // List<int[]> longer;
/* 275:335 */     if (l1.size() < l2.size())
/* 276:    */     {
/* 277:336 */       shorter = l1;
/* 278:337 */       longer = l2;
/* 279:    */     }
/* 280:    */     else
/* 281:    */     {
/* 282:339 */       shorter = l2;
/* 283:340 */       longer = l1;
/* 284:    */     }
/* 285:343 */     List<int[]> l3 = new ArrayList();
/* 286:    */     
/* 287:    */ 
/* 288:    */ 
/* 289:347 */     int pointer = 0;
/* 290:349 */     for (int i = 0; i < longer.size(); i++)
/* 291:    */     {
/* 292:351 */       int[] elem1 = (int[])longer.get(i);
/* 293:353 */       if (pointer >= shorter.size()) {
/* 294:354 */         l3.add(elem1);
/* 295:    */       }
/* 296:357 */       for (int j = pointer; j < shorter.size(); j++)
/* 297:    */       {
/* 298:360 */         int[] elem2 = (int[])shorter.get(j);
/* 299:363 */         if (elem2[0] == elem1[0])
/* 300:    */         {
/* 301:366 */           l3.add(elem2);
/* 302:367 */           pointer = j + 1;
/* 303:368 */           break;
/* 304:    */         }
/* 305:372 */         if (elem2[0] > elem1[0])
/* 306:    */         {
/* 307:374 */           l3.add(elem1);
/* 308:375 */           break;
/* 309:    */         }
/* 310:378 */         l3.add(elem2);
/* 311:379 */         pointer = j + 1;
/* 312:    */       }
/* 313:    */     }
/* 314:387 */     return l3;
/* 315:    */   }
/* 316:    */   
/* 317:    */   public double getNodeProb()
/* 318:    */   {
/* 319:392 */     double currProb = (this.nodeCount + 0.01D) / (TOTAL_TOKENS + 0.01D);
/* 320:393 */     return currProb;
/* 321:    */   }
/* 322:    */   
/* 323:    */   public int getSentenceOverlapCount(Node n1, Node n2)
/* 324:    */   {
/* 325:400 */     return getSetenceOverlap(n1, n2).size();
/* 326:    */   }
/* 327:    */   
/* 328:    */   public int hashCode()
/* 329:    */   {
/* 330:406 */     return this.nodeName.hashCode();
/* 331:    */   }
/* 332:    */   
/* 333:    */   public boolean equals(Object o)
/* 334:    */   {
/* 335:412 */     Node n = (Node)o;
/* 336:414 */     if (n.nodeName.equals(this.nodeName)) {
/* 337:415 */       return true;
/* 338:    */     }
/* 339:418 */     return false;
/* 340:    */   }
/* 341:    */ }