package in.succinct.defs.db.model.did.identifier;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;

public interface Did {
    
    @UNIQUE_KEY
    String getDid();
    void setDid(String did);
    
    
    String getName();
    void setName(String name);
    
}
