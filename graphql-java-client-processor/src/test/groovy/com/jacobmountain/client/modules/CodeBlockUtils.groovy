package com.jacobmountain.client.modules

import com.squareup.javapoet.CodeBlock

class CodeBlockUtils {

    static def renderBlocks(List<CodeBlock> blocks) {
        blocks.collect { block ->
            block.toString()
                    .replaceAll("\\t|\\n", "")
        }.join(";") + ";"
    }

}
