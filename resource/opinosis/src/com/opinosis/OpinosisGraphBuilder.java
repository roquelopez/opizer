/*   1:    */ package com.opinosis;
/*   2:    */ 
/*   3:    */ import java.io.BufferedReader;
/*   4:    */ import java.io.PrintStream;
/*   5:    */ import java.util.HashMap;
/*   6:    */ import java.util.HashSet;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.Set;
/*   9:    */ import org.jgrapht.graph.DefaultWeightedEdge;
/*  10:    */ import org.jgrapht.graph.SimpleDirectedWeightedGraph;
/*  11:    */ import org.textbug.utility.FileUtil;
import org.jgrapht.graph.DefaultWeightedEdge;
/*  12:    */ 
/*  13:    */ public class OpinosisGraphBuilder<T>
/*  14:    */   extends GraphBuilder
/*  15:    */ {
/*  16:    */   SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;
/*  17: 26 */   HashMap<String, Node> wordNodeMap = new HashMap();
/*  18: 29 */   private HashSet<String> mStopWords = new HashSet();
/*  19:    */   
/*  20:    */   public OpinosisGraphBuilder()
/*  21:    */   {
/*  22: 33 */     this.graph = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
/*  23: 34 */     loadStopWords();
/*  24:    */   }
/*  25:    */   
/*  26:    */   public SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> getGraph()
/*  27:    */   {
/*  28: 40 */     return this.graph;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public HashMap<String, Node> growGraph(String str, int docid, int sid)
/*  32:    */   {
/*  33: 48 */     return growSentenceGraph(str, docid, sid);
/*  34:    */   }
/*  35:    */   
/*  36:    */   private void loadStopWords()
/*  37:    */   {
/*  38: 54 */     String stopWord = "";
/*  39:    */     try
/*  40:    */     {
/*  41: 56 */       BufferedReader reader = 
/*  42: 57 */         FileUtil.getResourceFromPackage("com/opinosis/resources/stopwords_portugues.txt");
/*  43: 58 */       while ((stopWord = reader.readLine()) != null)
/*  44:    */       {
/*  45: 59 */         stopWord = stopWord.trim();
/*  46: 60 */         this.mStopWords.add(stopWord);
/*  47:    */       }
/*  48:    */     }
/*  49:    */     catch (Exception exception)
/*  50:    */     {
/*  51: 64 */       exception.printStackTrace();
/*  52: 65 */       System.exit(-1);
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   private HashMap growSentenceGraph(String str, int docid, int sid)
/*  57:    */   {
/*  58: 72 */     String[] words = str.split(" ");
/*  59: 73 */     Node prevVertex = null;
/*  60: 74 */     Node currVertex = null;
/*  61:    */     
/*  62: 76 */     boolean isPrevVertexNew = true;
/*  63: 77 */     boolean isCurrVertexNew = true;
/*  64: 78 */     int pos = 0;
/*  65: 83 */     for (int i = 0; i < words.length; i++)
/*  66:    */     {
/*  67: 85 */       String word = words[i].trim();
/*  68: 86 */       if (word.length() != 0)
/*  69:    */       {
/*  70: 91 */         isCurrVertexNew = true;
/*  71: 93 */         if (this.wordNodeMap.containsKey(word))
/*  72:    */         {
/*  73: 95 */           isCurrVertexNew = false;
/*  74: 96 */           currVertex = (Node)this.wordNodeMap.get(word);
/*  75: 97 */           currVertex.addDocId(docid);
/*  76: 98 */           currVertex.addSentenceId(sid, i);
/*  77:    */         }
/*  78:    */         else
/*  79:    */         {
/*  80:101 */           currVertex = new Node();
/*  81:102 */           currVertex.setNodeName(word);
/*  82:103 */           currVertex.addDocId(docid);
/*  83:104 */           currVertex.addSentenceId(sid, i);
/*  84:105 */           this.graph.addVertex(currVertex);
/*  85:106 */           isCurrVertexNew = true;
/*  86:107 */           this.wordNodeMap.put(word, currVertex);
/*  87:    */         }
/*  88:120 */         if ((isCurrVertexNew) || (isPrevVertexNew))
/*  89:    */         {
/*  90:121 */           if (prevVertex != null) {
/*  91:124 */             if ((!currVertex.equals(prevVertex)) && (canAdd(prevVertex))) {
/*  92:125 */               this.graph.addEdge(prevVertex, currVertex);
/*  93:    */             }
/*  94:    */           }
/*  95:    */         }
/*  96:    */         else
/*  97:    */         {
/*  98:134 */           DefaultWeightedEdge e = (DefaultWeightedEdge)this.graph.getEdge(prevVertex, currVertex);
/*  99:135 */           if (e == null)
/* 100:    */           {
/* 101:    */             try
/* 102:    */             {
/* 103:139 */               if ((currVertex.equals(prevVertex)) || (!canAdd(prevVertex))) {
/* 104:    */                 break;
/* 105:    */               }
/* 106:140 */               this.graph.addEdge(prevVertex, currVertex);
/* 107:    */             }
/* 108:    */             catch (IllegalArgumentException e1)
/* 109:    */             {
/* 110:144 */               System.err.println("Problem Linking '" + prevVertex + "'  and '" + currVertex + "'");
/* 111:    */             }
/* 112:    */           }
/* 113:    */           else
/* 114:    */           {
/* 115:150 */             double wt = 0.0D;
/* 116:151 */             wt = this.graph.getEdgeWeight(e) + 1.0D;
/* 117:152 */             this.graph.setEdgeWeight(e, wt);
/* 118:    */           }
/* 119:    */         }
/* 120:    */         label328:
/* 121:157 */         prevVertex = currVertex;
/* 122:158 */         isPrevVertexNew = isCurrVertexNew;
/* 123:    */       }
/* 124:    */     }
/* 125:161 */     return this.wordNodeMap;
/* 126:    */   }
/* 127:    */   
/* 128:    */   private boolean canAdd(Node prevVertex)
/* 129:    */   {
/* 130:166 */     if (prevVertex.getNodeName().matches("(\\./\\.|!/!|\\?/\\?)")) {
/* 131:167 */       return false;
/* 132:    */     }
/* 133:170 */     return true;
/* 134:    */   }
/* 135:    */   
/* 136:    */   public void printGraph()
/* 137:    */   {
/* 138:175 */     if (this.graph != null) {
/* 139:176 */       System.out.println(this.graph.toString());
/* 140:    */     }
/* 141:178 */     Set<Node> vertexSet = this.graph.vertexSet();
/* 142:    */     
/* 143:180 */     Iterator<Node> vIter = vertexSet.iterator();
/* 144:181 */     System.out.println("digraph{");
/* 145:    */     Iterator<DefaultWeightedEdge> edgeIterator;
/* 146:183 */     for (; vIter.hasNext(); edgeIterator.hasNext())
/* 147:    */     {
/* 148:184 */       Node v = (Node)vIter.next();
/* 149:185 */       Set<DefaultWeightedEdge> edges = this.graph.edgesOf(v);
/* 150:186 */       edgeIterator = edges.iterator();
/* 151:    */       
/* 152:188 */       //continue;
/* 153:    */       
/* 154:190 */       DefaultWeightedEdge e = (DefaultWeightedEdge)edgeIterator.next();
/* 155:191 */       String target = ((Node)this.graph.getEdgeTarget(e)).getNodeName();
/* 156:192 */       String source = ((Node)this.graph.getEdgeSource(e)).getNodeName();
/* 157:193 */       if (!target.equals(v.getNodeName())) {
/* 158:194 */         System.out.println(v.getNodeName() + "->" + target + ";");
/* 159:    */       }
/* 160:    */     }
/* 161:199 */     System.out.println("}");
/* 162:    */   }
/* 163:    */ }


