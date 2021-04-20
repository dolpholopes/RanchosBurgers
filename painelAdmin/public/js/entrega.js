let taxaRecuperadoBD;
let bd = firebase.firestore().collection("app").doc("taxa")

//==================================================== OUVINTE ====================================================
bd.onSnapshot(function(documento){
    const dados = documento.data()

    const taxa = document.getElementById("taxaEntrega")

	taxaRecuperadoBD = dados.taxa

    taxa.value = dados.taxa

})

function validarCampoTaxa(){
	
	const taxa = document.getElementById("taxaEntrega").value

	if(taxa.trim() == ""){
		abrirModalAlerta("Preencha o campo taxa de entrega!")
	}
	else if(taxaRecuperadoBD == taxa){
		abrirModalAlerta("Mensagem não foi alterada!")
	}
	else{
		abrirModalProgress()
		salvarDadosTaxa(taxa)
	}
}

function salvarDadosTaxa(taxa) {
	const dados = {
		taxa: taxa
	}
	bd.update(dados).then(function () {
		removerModalProgress()

	}).catch(function (error) {
		removerModalProgress()
		abrirModalAlerta("Erro ao Salvar Dados: " + error)
	})
}


//==================================================== MODAL PROGRESSBAR ====================================================

function abrirModalProgress() {
	$("#modalProgress").modal()
}

function removerModalProgress() {
	$("#modalProgress").modal("hide")
	window.setTimeout(function(){
		document.getElementById("modalProgress").click()
	},500)
}


//==================================================== MODAL ALERTA ====================================================
function abrirModalAlerta(mensagem) {
	$("#modalAlerta").modal()
	document.getElementById("alertaMenssagem").innerText = mensagem
}

//==================================================== LIMPAR CAMPOS ====================================================
function limparCampos(){
	document.getElementById("taxaEntrega").value = ""

}

//==================================================== PROXIMO CAMPO ====================================================
function proximoInput(id, evento){
    if (evento.keyCode == 13){
        document.getElementById(id).focus()
    }
}