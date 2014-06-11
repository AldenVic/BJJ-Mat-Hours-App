$(document).on('pageinit', function() {
	try {
		setProgress();
		setMatHours();
		setProfile();
		setStudents();
		if(localStorage["lastupdate"] != undefined) {
			$(".last-updated").text(localStorage["lastupdate"]);
		}
	}
	catch (err) {
		alert("Application error! Clearing cache and reseting app.\n" + err.message);
		localStorage.clear();
		$.mobile.changePage("#top-level");
	}
});

$(document).on('pagechange', function(event, ui) { 
	if(localStorage["students"] == undefined && ui.toPage.attr("data-url") !="select-user") {
		alert("No selected student found. Please choose one.");
		refreshStudents();
		$.mobile.changePage("#select-user");
	}	
});

$(document).on("pageshow", function(event, ui) {
	if($.mobile.activePage.attr("data-url") == "loading") {
		refreshData();
		$.mobile.changePage(ui.prevPage);
	}
});

//Refreshes
function refreshData() {
	try {
		refreshStudents();
		//refreshProfile();
		refreshMatHours();
		refreshProgress();
		refreshLastUpdated();
	}
	catch (err) {
		alert("Application error! Clearing cache and reseting app.\n" + err.message );
		localStorage.clear();
		$.mobile.changePage("#top-level");
	}
}
function refreshProfile() {
	if(localStorage["students"] != undefined && localStorage["user"] != undefined) {
		var students = JSON.parse(localStorage["students"]);
		var user = JSON.parse(localStorage["user"]);
		$(students.d).each(function() {
			if(this.barcode == user.barcode) {
				localStorage.setItem("user", JSON.stringify(this));
			}
		});
		
		setProfile();
	}
}

function refreshLastUpdated() {
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth()+1; //January is 0!
	var yyyy = today.getFullYear();
	var hh = today.getHours();
	var MM = today.getMinutes();
	var ss = today.getSeconds();
	
	if(dd<10){ dd='0'+dd; }  
	if(mm < 10){ mm='0'+mm; }  
	if(MM<10){ MM='0'+MM; }
	if(hh<10) { hh='0'+hh; }
	if(ss<10) {ss='0'+ss; }
	today = mm+'/'+dd +'/'+yyyy+" "+hh+":"+MM+":"+ss;
	
	localStorage.setItem("lastupdate", today);
	$(".last-updated").text(localStorage["lastupdate"]);
}

function refreshMatHours() {
	if(localStorage["user"] != undefined) {
		var user = JSON.parse(localStorage["user"]);
		var mathours = window.studentInfo.getMatHoursLog(user.barcode, $("#day-range").val());
		localStorage.setItem("mathours", mathours);
		setMatHours();
	}
}

function refreshStudents() {
	var students = window.studentInfo.getStudents();
	localStorage.setItem("students", students);
	setStudents();
}

function refreshProgress() {
	if(localStorage["user"] != undefined) {
		var user = JSON.parse(localStorage["user"]);
		var progress = 	window.studentInfo.getMatHoursProgress(user.barcode);
		localStorage.setItem("progress", progress);
		setProgress();
	}
}

//Setups
function setProgress() {
	if(localStorage["progress"] != undefined) {
		var progress = JSON.parse(localStorage["progress"]);
		var percent = ((progress.current_hours / progress.hours) * 100).toFixed(0);
		$("#progress-knob").html("");
		$("#progress-knob").html('<input class="knob" data-readOnly=true data-data-width="150" data-min="0" data-displayInput=false>');
		$(".knob").attr("data-max", progress.hours);
		$(".knob").val(progress.current_hours);
		$(".knob").attr("data-fgColor", progress.current_belt);
		$(".knob").knob();
		$("#num-hours").text(progress.current_hours);
		$("#hours-to-promotion").text(progress.hours);
		$("#percentage-to-promotion").text(percent + "%");
		$(".ui-listview").each(function() {
			$(this).listview("refresh");
		});
	}
}
function setMatHours() {
	if(localStorage["mathours"] != undefined) {
		$("#history-list").html("");
		$("#history-list").append("<li data-role='list-divider'><div class='ui-grid-b'><div class='ui-block-a'>Date</div><div class='ui-block-b'>Class</div><div class='ui-block-c'>Hours</div></div></li>");
		var json = JSON.parse(localStorage["mathours"]);
		
		$(json.d).each(function() {
			$("#history-list").append("<li><div class='ui-grid-b'><div class='ui-block-a'>" + this.timestamp + "</div><div class='ui-block-b'>" + this.reason + "</div><div class='ui-block-c'>" + this.num_hours + "</div></div></li>");
		});
		$(".ui-listview").each(function() {
			$(this).listview("refresh");
		});		
	}
}

function setStudents() {
	if(localStorage["students"] != undefined) {
		$("#user-list").html("");
		var json = JSON.parse(localStorage["students"]);
		$(json.d).each(function() {
			$("#user-list").append("<li><a data-corners='false' id='"+ this.barcode + "' data-role='button' onclick='selectUser(" + JSON.stringify(this) + ");'>" + this.name + "</a></li>");
		});
		$(".ui-listview").each(function() {
			$(this).listview("refresh");
		});		
	}
}

function setProfile() {
	if(localStorage["user"] != undefined) {
		$("#profile-list").html("");
		var user = JSON.parse(localStorage["user"]);
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>Name:</div><div class='ui-block-b'>" + user.name + "</div></div></li>");
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>E-mail Address:</div><div class='ui-block-b'>" + user.email + "</div></div></li>");		
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>Phone Number:</div><div class='ui-block-b'>" + user.phone + "</div></div></li>");
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>Belt Rank:</div><div class='ui-block-b'>" + user.belt + "</div></div></li>");
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>Stripes:</div><div class='ui-block-b'>" + user.stripes + "</div></div></li>");
		$("#profile-list").append("<li><div class='ui-grid-a'><div class='ui-block-a'>Last Promotion Date:</div><div class='ui-block-b'>" + user.last_promotion + "</div></div></li>");
		$(".ui-listview").each(function() {
			$(this).listview("refresh");
		});		
	}
}

//Misc
function selectUser(json) {
		localStorage.setItem("user", JSON.stringify(json));
		refreshData();
		setProfile();
		$.mobile.changePage("#top-level");
}



