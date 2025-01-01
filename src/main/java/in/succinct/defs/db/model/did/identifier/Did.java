package in.succinct.defs.db.model.did.identifier;

import com.venky.swf.db.annotations.column.COLUMN_SIZE;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;

public interface Did {
    
    @UNIQUE_KEY
    @COLUMN_SIZE(1024)
    String getDid();
    void setDid(String did);
    
    
    String getName();
    void setName(String name);
    
}
