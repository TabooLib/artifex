package ink.ptms.artifex.script

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

/**
 * Artifex
 * ink.ptms.artifex.script.VelocityArtifactDescription
 *
 * @author scorez
 * @since 4/22/24 22:39.
 */
class VelocityArtifactDescription(
    name: String,
    main: String,
    author: String,
    version: String
): ArtifactDescription(name, main, author, version) {

    override val file = Configuration.empty(Type.JSON)

}