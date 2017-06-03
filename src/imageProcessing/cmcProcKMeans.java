package imageProcessing;

import generalStatPurpose.gpFrequencyDistributionStatFunctions;



public class cmcProcKMeans {

    class kMeanItem
    {
        int value;
        int cluster;
        kMeanItem(int i)
        {
            value=i;
            cluster=-1;
        }
    }
    
    private kMeanItem[] kmset=null;
    private int aantalClusters=-1;
    private int[] centroids;      // in feite MEAN
    
    private gpFrequencyDistributionStatFunctions pstat=null;
    
    //------------------------------------------------------------
    cmcProcKMeans()
    //------------------------------------------------------------
    {
      pstat = new gpFrequencyDistributionStatFunctions();    
    }
       
    
    //------------------------------------------------------------
    public void populateSingleDimensionSet(int iaantal , int[] iset)
    //------------------------------------------------------------
    {
      int aantal = iset.length;
      kmset = new kMeanItem[aantal];
      for(int i=0;i<aantal;i++) kmset[i] = new kMeanItem(iset[i]);
      //
      aantalClusters=iaantal;
      centroids = new int[aantalClusters];
      
      /*
      centroids[0] = 0;
      centroids[1] = pstat.getMode( iset );
      for(int i=2;i<aantalClusters;i++)
      {
          int per = centroids[1] / (aantalClusters - 1);
          centroids[i] = centroids[i-1] - per;
      }
      */
      // SEED  - dit werkt best
      for(int i=0;i<aantalClusters;i++)
      {
          centroids[i] = (i+1) *10;
      }
      
      sho();
    }
    
    //------------------------------------------------------------
    private void sho()
    //------------------------------------------------------------
    {
       String sLijn = "CENTROIDS : ";    
       for(int i=0;i<aantalClusters;i++) {
           sLijn += "\n  (" + i + ") " + centroids[i] + " ";
           sLijn += " OBS="+getNumberOfElementsPerCentroidViaIdx(i);
       }
       //System.out.println(sLijn);
    }
    
    //------------------------------------------------------------
    public void doit()
    //------------------------------------------------------------
    {
        startIteratie();
        for(int i=0;i<10;i++)
        {
        if( itereer() == false) break;
        }
    }
    
    //------------------------------------------------------------
    private void startIteratie()
    //------------------------------------------------------------
    {
        int[] prevCentroids = new int[aantalClusters];
        int[] afstand = new int[aantalClusters];
        for(int i=0;i<aantalClusters;i++) prevCentroids[i] = centroids[i];
        // loop doorheen alle waarden en alloceer
        for(int i=0;i<kmset.length;i++)
        {
            for(int j=0;j<aantalClusters;j++)
            {
                afstand[j] = centroids[j] - kmset[i].value;
                if( afstand[j] < 0 ) afstand[j] = 0 - afstand[j];
            }
            // zoek kleinste afstand
            int idx=0;
            int min=afstand[idx];
            for(int j=1;j<aantalClusters;j++)
            {
              if( afstand[j] < min ) {
                  min = afstand[j];
                  idx=j;
              }
            }
            kmset[i].cluster = idx;
            // nu de MEAN op die cluster aanpassen -> andere centroid
            int som=0;
            int nn=0;
            for(int z=0;z<kmset.length;z++)
            {
                if( kmset[z].cluster != idx ) continue;
                som += kmset[i].value;
                nn++;
            }
            centroids[idx] = som/nn;
        }
        sho();
    }
    
    //------------------------------------------------------------
    private boolean itereer()
    //------------------------------------------------------------
    {
        boolean swap=false;
        int[] afstand = new int[aantalClusters];
        //
        for(int i=0;i<kmset.length;i++)
        {
            for(int j=0;j<aantalClusters;j++)
            {
                afstand[j] = centroids[j] - kmset[i].value;
                if( afstand[j] < 0 ) afstand[j] = 0 - afstand[j];
            }
            // zoek kleinste afstand
            int idx=0;
            int min=afstand[idx];
            for(int j=1;j<aantalClusters;j++)
            {
              if( afstand[j] < min ) {
                  min = afstand[j];
                  idx=j;
              }
            }
            if( idx != kmset[i].cluster ) {
                swap=true;
                int oldidx = kmset[i].cluster;
                kmset[i].cluster = idx;
                // nu de MEAN op die cluster aanpassen -> andere centroid
                int som=0;
                int nn=0;
                for(int z=0;z<kmset.length;z++)
                {
                    if( kmset[z].cluster != idx ) continue;
                    som += kmset[z].value;
                    nn++;
                }
                centroids[idx] = som/nn;
                // de mean op de vrorgere cluster aanpassen
                som=0;
                nn=0;
                idx = oldidx;
                for(int z=0;z<kmset.length;z++)
                {
                    if( kmset[z].cluster != idx ) continue;
                    som += kmset[z].value;
                    nn++;
                }
                
            }
        }
        sho();
        return swap;
    }
    
    //------------------------------------------------------------
    public int[] getClusters()
    //------------------------------------------------------------
    {
    	int[] clusterlist = new int[kmset.length];
    	for(int i=0;i<kmset.length;i++ ) clusterlist[i] = kmset[i].cluster;
    	return clusterlist;
    }
    //------------------------------------------------------------
    public int getClusterViaIdx(int idx)
    //------------------------------------------------------------
    {
        if( (idx >= kmset.length) || (idx<0) ) return -1;
        return kmset[idx].cluster;
    }
    //------------------------------------------------------------
    public int getCentroidValueViaIdx(int idx)
    //------------------------------------------------------------
    {
    	 if( (idx >= centroids.length) || (idx<0) ) return -1;
    	 return centroids[idx];
    }
    //------------------------------------------------------------
    public int getNumberOfElementsPerCentroidViaIdx(int idx)
    //------------------------------------------------------------
    {
    	 int teller=0;
    	 if( (idx >= centroids.length) || (idx<0) ) return -1;
    	 for(int j=0;j<kmset.length;j++) {
             if( kmset[j].cluster == idx) teller++;
         }
    	 return teller;
    }
}
