package com.dajodi.scandic;


import java.io.InputStream;
import java.net.URI;

public interface ScandicStreamProvider {

    InputStream get(URI resource);
}
