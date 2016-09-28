import org.apache.sling.pipes.Plumber;
import org.apache.sling.pipes.Pipe;

Plumber plumber = getService(Plumber.class);

res = resourceResolver.getResource("/etc/packages/netcentric/sample-package-0.0.1-SNAPSHOT.zip/jcr:content/vlt:definition/upgrader/test-sling-pipes/pipe")
Pipe pipe = plumber.getPipe(res)
pipe.getOutput().each { res ->
    println res.path
}
