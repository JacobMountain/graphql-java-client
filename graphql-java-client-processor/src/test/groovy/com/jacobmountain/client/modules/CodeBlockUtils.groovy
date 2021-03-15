package com.jacobmountain.client.modules

import com.squareup.javapoet.CodeBlock

class CodeBlockUtils {

    static def renderBlocks(Optional<CodeBlock> blocks) {
        blocks.get().toString()
                .replaceAll("\\t|\\n", "") + ";"
    }

}
