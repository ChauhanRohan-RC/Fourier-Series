package models;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DirStat {

    private final AtomicInteger dirs = new AtomicInteger(0);

    private final AtomicInteger successFiles = new AtomicInteger(0);
    private final AtomicInteger failFiles = new AtomicInteger(0);

    private final AtomicLong size = new AtomicLong(0L);

    public DirStat addDir() {
        dirs.incrementAndGet();
        return this;
    }

    public int dirs() {
        return dirs.get();
    }

    public DirStat addSuccessFIle(long size) {
        successFiles.incrementAndGet();
        this.size.addAndGet(size);
        return this;
    }

    public DirStat addFailedFIle(long size) {
        failFiles.incrementAndGet();
        this.size.addAndGet(size);
        return this;
    }

    public DirStat addFile(boolean success, long size) {
        return success? addSuccessFIle(size): addFailedFIle(size);
    }

    public int successFiles() {
        return successFiles.get();
    }

    public int failedFiles() {
        return failFiles.get();
    }
}
