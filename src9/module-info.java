module dorkbox.cabParser {
    exports dorkbox.cabParser;
    exports dorkbox.cabParser.decompress;
    exports dorkbox.cabParser.decompress.lzx;
    exports dorkbox.cabParser.decompress.none;
    exports dorkbox.cabParser.decompress.zip;
    exports dorkbox.cabParser.extractor;
    exports dorkbox.cabParser.structure;


    requires transitive dorkbox.byteUtils;
    requires transitive dorkbox.updates;
    requires transitive dorkbox.utilities;
}
