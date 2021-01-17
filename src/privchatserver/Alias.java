//Grevenitis Ioannis icsd13045
//Papaloukas Thomas icsd14155

package privchatserver;

//Klasi gia ta pseudonyma twn xristwn. Idiotites einai ena string (to pseudonymo) kai mia boolean metavliti gia na elegxoume an to pseudonymo xrisimopoieitai idi.
public class Alias {
    private String alias;
    private boolean inUse;
    
    //Constructors
    public Alias(){
        this.alias = null;
        this.inUse = false;
    }
    
    public Alias(String alias){
       this.alias = alias;
       this.inUse = false;
    }
    
    //Getters
    public String getAlias(){
        return alias;
    }
    
    public boolean getInUse(){
        return inUse;
    }
    
    //Methodos gia allagi diathesimotitas pseudonymou
    public void changeAvailability(boolean avail){
        this.inUse = avail;
    }
    
    @Override
    public String toString(){
        return alias + " " + inUse;
    }
}
