package com.app.research.good_gps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.app.research.good_gps.model.Club
import com.app.research.good_gps.model.Clubs
import com.app.research.good_gps.model.ForeGolfTemp
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.singlescreen_r_d.skaifitness.VStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            VStack(8.dp) {
                CenterAlignedTopAppBar(
                    title = { Text("Courses") },
                    navigationIcon = {
                        IconButton(onClick = { navHostController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        ClubsContent(
            clubs = ForeGolfTemp.clubs.filterClubs(searchQuery),
            modifier = Modifier.padding(innerPadding),
            onClick = { navHostController.navigate("Courses") }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFE5E5EA) // iOS search bar background
    ) {
        HStack(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF8E8E93), // iOS secondary label
                modifier = Modifier.size(18.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF000000), // iOS black
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Color(0xFF007AFF)), // iOS blue cursor
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search courses...",
                            fontSize = 16.sp,
                            color = Color(0xFF8E8E93), // iOS placeholder color
                            fontWeight = FontWeight.Normal
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubsContent(
    clubs: Clubs,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* No-op */ }
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = clubs.clubs) { item ->
            Club(item, onClick = onClick)
        }
    }
}

@Preview
@Composable
private fun Club(
    club: Club = ForeGolfTemp.club,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* No-op */ }
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2F2F7), // iOS system background
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = club.clubName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF000000), // iOS black
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Location Info - iOS style
            HStack(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF8E8E93), // iOS secondary label
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${club.city}, ${club.state}",
                    fontSize = 15.sp,
                    color = Color(0xFF8E8E93), // iOS secondary label
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Distance - iOS style
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF007AFF).copy(alpha = 1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${club.distance} ${club.measureUnit.uppercase()}",
                        fontSize = 13.sp,
                        color = Color.White, // White text on blue background
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }


            Spacer(modifier = Modifier.height(4.dp))

            // Address - iOS style
            Text(
                text = club.address,
                fontSize = 13.sp,
                color = Color(0xFF8E8E93), // iOS secondary label
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Courses Section - iOS style
            Text(
                text = "Courses",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000), // iOS black
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Courses List - iOS style
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {
                repeat(club.courses.size) { index ->
                    CourseChip(course = club.courses[index])
                }
            }
        }
    }
}

@Composable
private fun CourseChip(course: com.app.research.good_gps.model.Course) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (course.hasGPS == 1)
            Color(0xFF34C759).copy(alpha = 0.1f) // iOS green with opacity
        else
            Color(0xFFFF9500).copy(alpha = 0.1f), // iOS orange with opacity
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // GPS Status Indicator - iOS style
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (course.hasGPS == 1) Color(0xFF34C759) else Color(0xFFFF9500)
                    )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = course.courseName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000), // iOS black
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Text(
                    text = "${course.numHoles} holes",
                    fontSize = 11.sp,
                    color = Color(0xFF8E8E93), // iOS secondary label
                    lineHeight = 16.sp
                )
            }
        }
    }
}