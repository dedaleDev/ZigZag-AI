// Charger la liste des utilisateurs
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
          const userItem = document.createElement("div");
          userItem.classList.add("user-item");
          userItem.innerHTML = `
            <span>${user}</span>
            <button>&times;</button>
          `;
          document.querySelector('.user-list').appendChild(userItem);
          //ce qui créer le code html suivant pour chaque utilisateur :
      });

      // Gestion de la suppression des utilisateurs
      document.querySelectorAll('.user-item button').forEach(button => {
        button.addEventListener('click', (e) => {
          const userItem = e.target.closest('.user-item');
          const userName = userItem.querySelector('span').textContent;

          if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur ${userName} ?`)) {
            fetch('/api/deleteUser', {
              method: 'POST',
              headers: {
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify({ name: userName })
            })
            .then(response => response.json())
            .then(data => {
                  console.log(data.message); // Affiche le message de réponse
                  if (data.status === 'success') {
                    userItem.remove();
                  } else {
                    alert('Erreur lors de la suppression de l\'utilisateur.');
                  }
            })
            .catch((error) => {
              console.error('Error:', error);
              alert('Erreur lors de la suppression de l\'utilisateur.');
            });
          }
        });
      });
})
.catch((error) => {
  console.error('Error:', error);
});

// Charger la liste des voix
fetch('/api/getVoiceList', {
  method: 'GET',
  headers: {
      'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
      console.log(data.voices); // Affiche la liste des voix
      const voices = data.voices; // La liste des voix est déjà un tableau
      const voiceSelect = document.getElementById('voice-select');
      voices.forEach(voice => {
          const option = document.createElement("option");
          option.value = voice;
          option.textContent = voice;
          voiceSelect.appendChild(option);
      });
})
.catch((error) => {
  console.error('Error:', error);
});

// Charger le port série Arduino
fetch('/api/getArduinoCom', {
  method: 'POST',
  headers: {
      'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
      console.log(data.comArduino); // Affiche le port série Arduino
      document.getElementById('arduino-port').value = data.comArduino;
})
.catch((error) => {
  console.error('Error:', error);
});

// Gestion du bouton de sauvegarde
document.querySelector('.save-btn').addEventListener('click', () => {
  const selectedVoice = document.getElementById('voice-select').value;
  const arduinoPort = document.getElementById('arduino-port').value;

  // Sauvegarder la voix sélectionnée
  fetch('/api/selectVoiceAI', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({ voice: selectedVoice })
  })
  .then(response => response.json())
  .then(data => {
        console.log(data.message); // Affiche le message de réponse
  })
  .catch((error) => {
    console.error('Error:', error);
  });

  // Sauvegarder le port série Arduino
  fetch('/api/selectArduinoCom', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({ comArduino: arduinoPort })
  })
  .then(response => response.json())
  .then(data => {
        console.log(data.message); // Affiche le message de réponse
  })
  .catch((error) => {
    console.error('Error:', error);
  });

  alert('Paramètres enregistrés avec succès !');
});