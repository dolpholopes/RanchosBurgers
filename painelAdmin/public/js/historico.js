let tabela = document.getElementById("tabelaPedidos").getElementsByTagName("tbody")[0];

let bd = firebase.firestore().collection("pedidos");

let pedidoSelecionadoFinalizarPedido;

let pedidoSelecionadoCliente;

let keyLista = []


//==================================================== OUVINTE ====================================================
bd.where("pedido_status", "==", "finalizado").onSnapshot(function (documentos) {

	documentos.docChanges().forEach(function (changes) {

		if (changes.type === "added") {			
			const doc = changes.doc
			const dados = doc.data()
			keyLista.push(dados.pedido_id)
			criarItensTabela(dados)
		}
		else if (changes.type === "modified") {
			const doc = changes.doc
			const dados = doc.data()
		}
		else if (changes.type === "removed") {
			const doc = changes.doc
			const dados = doc.data()
			removerItensTabela(dados)
		}
	})
})


//==================================================== TABELA ==================================================== 
//---- adicionando itens tabela
function criarItensTabela(dados) {

	const linha = tabela.insertRow()

	const colunaClienteNome = linha.insertCell(0)
	const colunaPedidoDados = linha.insertCell(1)
	const colunaPedidoHora = linha.insertCell(2)

	const dados_pedido = dados.pedido_dados.substr(0, 10) + " ..."

	colunaClienteNome.appendChild(document.createTextNode(dados.cliente_nome))
	colunaPedidoDados.appendChild(document.createTextNode(dados_pedido.replace(/<br>/g, "  ")))

	const data = new Date(Number(dados.pedido_data))
	const date = moment(data).format('DD/MM/yyyy')
	colunaPedidoHora.appendChild(document.createTextNode(date))

	colunaClienteNome.style = "text-align: center"
	colunaPedidoDados.style = "text-align: center"
	colunaPedidoHora.style = "text-align: center"

    ordemDecrescente()
	criarBotoesTabela(linha, dados)

	//ordemCrescente()
}

//---- removendo itens tabela
function removerItensTabela(dados) {

	const index = keyLista.indexOf(dados.pedido_id)

	tabela.rows[index].remove()
	keyLista.splice(index, 1)
}

//---- criar botoes tabela
function criarBotoesTabela(linha, dados) {

	const colunaPedidoInf = linha.insertCell(3)


	const buttonDetalhesPedido = document.createElement("button")
	buttonDetalhesPedido.innerHTML = ` <i class="fas fa-info"></i> `
	buttonDetalhesPedido.className = "btn btn-outline-success btn-xs"
	buttonDetalhesPedido.style = "margin: auto; display: block;"


	buttonDetalhesPedido.onclick = function () {
		clickDetalhePedido(dados)
		return false
	}

	colunaPedidoInf.appendChild(buttonDetalhesPedido)
}


// ===============================  BOTÃO DETALHES PEDIDO =======================================
function clickDetalhePedido(dados) {
	$("#modalPedido").modal()

	const id = document.getElementById("pedidoID")
	const pedido_dados = document.getElementById("pedidoDados")
	const pedido_pagamento = document.getElementById("pedidoPagamento")
	const pedido_data = document.getElementById("pedidoData")

	const data = new Date(Number(dados.pedido_data))
	const date = moment(data).format('HH:mm:ss')


	id.innerHTML = dados.pedido_id
	pedido_dados.innerHTML = dados.pedido_dados
	pedido_pagamento.innerHTML = "Valor do pedido: " + dados.pedido_valor + "<br><br>" + dados.pedido_forma_pagamento
	pedido_data.innerHTML = date

}


//==================================================== MODAL PROGRESSBAR ====================================================
function abrirModalProgress() {
	$("#modalProgress").modal()
}

function removerModalProgress() {
	$("#modalProgress").modal("hide")
	window.setTimeout(function () {
		document.getElementById("modalProgress").click()
	}, 500)
}


//==================================================== MODAL ALERTA ====================================================
function abrirModalAlerta(mensagem) {
	$("#modalAlerta").modal()
	document.getElementById("alertaMenssagem").innerText = mensagem
}


//==================================================== FUNÇOES TABELA ====================================================
//-----------------------------pesquisa por id e nome
function pesquisar(opcao) {

	let inputValor, filtro, tr, td, i, valorItemTabela;
	inputValor = document.getElementById("pesquisar" + opcao).value;
	filtro = inputValor.toUpperCase()
	tr = tabela.getElementsByTagName("tr")

	for (i = 0; i < tr.length; i++) {
		td = tr[i].getElementsByTagName("td")[opcao]

		if (td) {
			valorItemTabela = td.textContent.toUpperCase()

			if (valorItemTabela.indexOf(filtro) == -1) {
				tr[i].style.display = "none"
			} else {
				tr[i].style.display = ""
			}
		}
	}
}


//==================================================== PAGINAÇÃO ====================================================
$("#maxRows").on("change", function () {

	let maxRows, tr, i;

	maxRows = parseInt($("#maxRows").val()) - 1
	tr = tabela.getElementsByTagName("tr")

	for (i = 0; i < tr.length; i++) {
		if (i > maxRows) {
			tr[i].style.display = "none"
		} else {
			tr[i].style.display = ""
		}
	}

	//----------paginação inserindo botoes
	$("#pagination").html("")

	let rows = maxRows + 1
	let totalRows = tr.length

	if (totalRows > rows) {
		let numpage = Math.ceil(totalRows / rows)
		for (let i = 1; i <= numpage; i++) {
			$("#pagination").append(' <li class="page-item">   <a class="page-link" href="#" >' + i + '</a></li> ').show()
		}
	}

	//----------paginação  click
	$("#pagination").on("click", function (e) {

		let numpage = parseInt(e.target.innerText)

		i = 1
		$("#tabelaCategoria tr:gt(0)").each(function () {

			if (i > (rows * numpage) || i <= ((rows * numpage) - rows)) {
				$(this).hide()
			} else {
				$(this).show()
			}
			i++;
		})
	})
})


function ordemDecrescente() {

	let tr = tabela.getElementsByTagName('tr')

	for (let i = 0; i < tr.length - 1; i++) {

		for (let j = 0; j < tr.length - (i + 1); j++) {

			let informacao1 = tr[j].getElementsByTagName("td")[2].textContent // 200
			let informacao2 = tr[j + 1].getElementsByTagName("td")[2].textContent // 152


			if (Number(informacao1.replace(/[^0-9\.]+/g, "")) < Number(informacao2.replace(/[^0-9\.]+/g, ""))) {

				tabela.insertBefore(tr.item(j + 1), tr.item(j))

				let valor = keyLista[j+1]
				keyLista[j + 1] = keyLista[j]
				keyLista[j] = valor
			}
		}
	}
}