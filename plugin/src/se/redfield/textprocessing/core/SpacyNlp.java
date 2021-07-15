/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
import org.knime.python2.kernel.PythonIOException;

public class SpacyNlp {
	private static final String VAR_NLP = "nlp";
	private static final String VAR_RES = "res";

	private final PythonContext context;
	private ExecutionContext exec;

	public SpacyNlp(PythonContext context, ExecutionContext exec, String spacyModel)
			throws PythonIOException, CanceledExecutionException {
		this.context = context;
		this.exec = exec;

		init(spacyModel);
	}

	private void init(String spacyModel) throws PythonIOException, CanceledExecutionException {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from SpacyNlp import SpacyNlp");
		b.a(VAR_NLP).a(" = SpacyNlp(").asr(spacyModel).a(")").n();
		context.executeInKernel(b.toString(), exec);
	}

	public List<TaggedEntity> computeNeTags(String sentece) throws PythonIOException, CanceledExecutionException {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder();
		b.a(VAR_RES).a(" = ").a(VAR_NLP).a(".ne_tag_sentence(").as(sentece).a(")").n();

		context.executeInKernel(b.toString(), exec);
		BufferedDataTable result = context.getDataTable(VAR_RES, exec, exec);

		List<TaggedEntity> tags = new ArrayList<>();

		for (DataRow row : result) {
			String text = row.getCell(0).toString();
			String tag = row.getCell(1).toString();
			tags.add(new TaggedEntity(text, tag));
		}

		return tags;
	}

	public BufferedDataTable processDocuments(BufferedDataTable inTable, String column, String method)
			throws PythonIOException, CanceledExecutionException {
		context.putDataTable(inTable, exec);
		context.executeInKernel(getProcessDocumentsScript(method, column), exec);
		return context.getDataTable(exec, exec);
	}

	private String getProcessDocumentsScript(String method, String column) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder();
		b.a(PythonContext.VAR_OUTPUT_TABLE).a(" = ").a(VAR_NLP).a(".").a(method).a("(").n();

		b.a("input_table = ").a(PythonContext.VAR_INPUT_TABLE).a(",").n();
		b.a("column = ").as(column).a(",").n();

		b.a(")");

		return b.toString();
	}
}
