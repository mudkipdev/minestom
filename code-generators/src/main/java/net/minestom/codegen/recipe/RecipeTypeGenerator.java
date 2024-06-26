package net.minestom.codegen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

public class RecipeTypeGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeTypeGenerator.class);
    private final InputStream recipeTypesFile;
    private final File outputFolder;

    public RecipeTypeGenerator(@Nullable InputStream recipeTypesFile, @NotNull File outputFolder) {
        this.recipeTypesFile = recipeTypesFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (recipeTypesFile == null) {
            LOGGER.error("Failed to find recipe_types.json.");
            LOGGER.error("Stopped code generation for recipe types.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }

        // Important classes we use alot
        JsonArray recipeTypes = GSON.fromJson(new InputStreamReader(recipeTypesFile), JsonArray.class);
        ClassName recipeTypeCN = ClassName.get("net.minestom.server.recipe", "RecipeType");
        TypeSpec.Builder recipeTypeEnum = TypeSpec.enumBuilder(recipeTypeCN)
                .addSuperinterface(ClassName.get("net.minestom.server.registry", "StaticProtocolObject"))
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());
        ClassName namespaceIdCN = ClassName.get("net.minestom.server.utils", "NamespaceID");

        ClassName networkBufferCN = ClassName.get("net.minestom.server.network", "NetworkBuffer");
        ParameterizedTypeName networkBufferTypeCN = ParameterizedTypeName.get(networkBufferCN.nestedClass("Type"), recipeTypeCN);

        // Fields
        recipeTypeEnum.addFields(
                List.of(
                        FieldSpec.builder(networkBufferTypeCN, "NETWORK_TYPE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                .initializer("$T.Enum($T.class)", networkBufferCN, recipeTypeCN)
                                .build(),
                        FieldSpec.builder(namespaceIdCN, "namespace", Modifier.PRIVATE, Modifier.FINAL).build()
                )
        );

        // Methods
        recipeTypeEnum.addMethods(
                List.of(
                        // Constructor
                        MethodSpec.constructorBuilder()
                                .addParameter(ParameterSpec.builder(namespaceIdCN, "namespace").addAnnotation(NotNull.class).build())
                                .addStatement("this.namespace = namespace")
                                .build(),
                        MethodSpec.methodBuilder("namespace")
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(NotNull.class)
                                .addAnnotation(Override.class)
                                .returns(namespaceIdCN)
                                .addStatement("return this.namespace")
                                .build(),
                        MethodSpec.methodBuilder("id")
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.INT)
                                .addAnnotation(Override.class)
                                .addStatement("return this.ordinal()")
                                .build()
                )
        );

        // Use data
        for (JsonObject recipeTypeObject : StreamSupport.stream(recipeTypes.spliterator(), true).map(JsonElement::getAsJsonObject).sorted(Comparator.comparingInt(o -> o.get("id").getAsInt())).toList()) {
            String recipeTypeName = recipeTypeObject.get("name").getAsString();
            recipeTypeEnum.addEnumConstant(recipeTypeConstantName(recipeTypeName), TypeSpec.anonymousClassBuilder(
                            "$T.from($S)",
                            namespaceIdCN, recipeTypeName
                    ).build()
            );
        }

        // Write files to outputFolder
        writeFiles(
                List.of(
                        JavaFile.builder("net.minestom.server.recipe", recipeTypeEnum.build())
                                .indent("    ")
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }

    private static @NotNull String recipeTypeConstantName(@NotNull String name) {
        return toConstant(name).replace("CRAFTING_", "");
    }
}
