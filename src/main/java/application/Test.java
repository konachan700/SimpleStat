package application;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        FileSystems.getDefault().getFileStores().forEach(it -> {
            System.out.println(it.type() + "  " + it.name() );

        });

//        FileSystems.getDefault().getFileStores()
//
//                .forEach(it -> {
//            System.out.println(it.name());
//                });
    }
}
