package in.succinct.defs.controller;

import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

public class ServicesController extends AbstractDirectoryController<VerificationMethod> {
    public ServicesController(Path path) {
        super(path);
    }
}
