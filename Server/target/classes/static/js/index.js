const messagesContainer = document.getElementById("messages");
const chatInput = document.getElementById("chat-input");
const sendBtn = document.getElementById("send-btn");
const nameSelect = document.getElementById("name-select");

// Fonction pour créer une nouvelle bulle de message
function createMessage(content, type) {
  const messageDiv = document.createElement("div");
  messageDiv.classList.add("message", type === "user" ? "user-message" : "bot-message");
  messageDiv.textContent = content;
  messagesContainer.appendChild(messageDiv);
  messagesContainer.scrollTop = messagesContainer.scrollHeight; // Scroll vers le bas
  return messageDiv;
}

// Fonction pour créer une bulle de message d'attente
function createLoadingMessage() {
  const loadingDiv = document.createElement("div");
  loadingDiv.classList.add("message", "bot-message");
  loadingDiv.textContent = "ZigZag: ...";
  messagesContainer.appendChild(loadingDiv);
  messagesContainer.scrollTop = messagesContainer.scrollHeight; // Scroll vers le bas
  return loadingDiv;
}

// Événement pour envoyer un message
sendBtn.addEventListener("click", () => {
  const userMessage = chatInput.value.trim();
  const selectedName = nameSelect.value;
  if (userMessage) {
    createMessage(`${selectedName}: ${userMessage}`, "user"); // Ajout du message utilisateur avec le nom
    chatInput.value = ""; // Efface l'input

    const loadingMessage = createLoadingMessage(); // Affiche le message d'attente

    fetch('/api/request', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: selectedName, message: userMessage })
      })
      .then(response => response.json())
      .then(data => {
        messagesContainer.removeChild(loadingMessage); // Supprime le message d'attente
        createMessage(`ZigZag: ${data.message}`, "bot"); // Affiche le message reçu
      })
      .catch((error) => {
        console.error('Error:', error);
        messagesContainer.removeChild(loadingMessage); // Supprime le message d'attente en cas d'erreur
        createMessage("ZigZag: Une erreur s'est produite.", "bot"); // Affiche un message d'erreur
    });
  }
});

// Permet d'envoyer un message avec la touche Entrée
chatInput.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    sendBtn.click();
  }
});


// Récupère la liste des utilisateurs
fetch('/api/getUsers', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json'
    }
})
.then(response => response.json())
.then(data => {
        console.log(data.users); // Affiche la liste des utilisateurs
        const users = data.users; // La liste des utilisateurs est déjà un tableau
        users.forEach(user => {
            console.log(user); // Affiche chaque utilisateur
            const option = document.createElement("option");
            option.value = user;
            option.textContent = user;
            nameSelect.appendChild(option);
        });
        if (users.length === 0) {
          alert("Il n'y a pas encore d'utilisateurs enregistrés, vous pouvez en créer un avec l'Arduino.")
          document.querySelector('.name-select').remove();
        }
})
.catch((error) => {
    console.error('Error:', error);
});
