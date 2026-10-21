package com.example.sociable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.example.sociable.ui.cadastro.CadastroScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

// Definição das cores
val DarkPurple = Color(0xFF1E1830)
val TextPink = Color(0xFFD97A8B)
val ButtonPink = Color(0xFFC95068)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SociableApp()
            }
        }
    }
}


@Composable
fun SociableApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("login") {
            WelcomeScreen(
                onCriarConta = {
                    navController.navigate("cadastro")
                },
                onEntrar = {
                    navController.navigate("entrar")
                }
            )
        }


        composable("cadastro") {
            CadastroScreen(
                onCriarConta = { nome, email, senha ->

                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(
                            email.trim(),
                            senha
                        )
                        .addOnSuccessListener { result ->

                            val profileUpdates = userProfileChangeRequest {
                                displayName = nome.trim()
                            }

                            result.user?.updateProfile(profileUpdates)
                                ?.addOnCompleteListener { profileTask ->
                                    if (!profileTask.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Conta criada, mas não foi possível salvar o nome.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Conta criada com sucesso!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                context,
                                exception.localizedMessage
                                    ?: "Erro ao criar conta.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                onEntrar = {
                    navController.navigate("entrar")
                }
            )
        }


        composable("entrar") {
            // Tela de login será implementada depois.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tela de login em desenvolvimento")
            }
        }
    }
}


@Composable
fun SplashScreen(navController: NavController) {
    // Interação: Aguarda 3 segundos e navega para a tela de login
    LaunchedEffect(Unit) {
        delay(3000L)
        navController.navigate("login") {
            // Remove a splash da pilha para o usuário não voltar pra ela ao apertar "Voltar"
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPurple),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Substitua R.drawable.ic_launcher_foreground pelo nome da sua logo importada
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = "Logo Sociable",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SOCIABLE",
            color = TextPink,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Conexões em Círculo ✨",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Indicador de carregamento simples
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(TextPink.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onCriarConta: () -> Unit,
    onEntrar: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPurple)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Logo menor
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Bem-vindo ao", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Sociable", color = TextPink, fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Encontre pessoas para curtir festas, eventos e rolês.\nSem compromisso, só diversão. \uD83C\uDF89",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Card de Evento de Exemplo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.DarkGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Barzinho do Centro · Vila ...", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Amanhã · 21h · 15 pessoas", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botão Gmail
        Button(
            onClick = { Toast.makeText(context, "Login com Google clicado", Toast.LENGTH_SHORT).show() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Entrar / Cadastrar com Gmail", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("ou", color = Color.White.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))

        // Botão Criar Conta


        Button(
            onClick = onCriarConta,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Criar minha conta 🎉",
                color = Color.White
            )
        }



        Spacer(modifier = Modifier.height(16.dp))

        // Botão Já tenho conta

        OutlinedButton(
            onClick = onEntrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = null
        ) {
            Text("Já tenho conta · Entrar")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ao continuar, você concorda com nossos Termos de Uso e Política de Privacidade",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
