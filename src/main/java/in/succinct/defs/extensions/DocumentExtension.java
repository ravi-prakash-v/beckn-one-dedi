package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.ModelOperationExtension;
import com.venky.swf.exceptions.AccessDeniedException;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import org.apache.lucene.index.DocIDMerger.Sub;

public class DocumentExtension extends ModelOperationExtension<Document> {
    static {
        registerExtension(new DocumentExtension());
    }
    
    @Override
    protected void afterSave(Document instance) {
        super.afterSave(instance);
        incrementModCount(instance);
    }
    @Override
    protected void afterDestroy(Document instance) {
        super.afterDestroy(instance);
        incrementModCount(instance);
    }
    private void incrementModCount(Document instance){
        Subject subject = instance.getSubject();
        subject.getModCount().increment();
        subject.save();
    }
    
}
