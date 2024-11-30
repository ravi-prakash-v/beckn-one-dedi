package in.succinct.defs.controller;

import com.venky.swf.path.Path;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.VerificationMethod;

public class ServicesController extends AbstractDirectoryController<Service> {
    public ServicesController(Path path) {
        super(path);
    }
}
