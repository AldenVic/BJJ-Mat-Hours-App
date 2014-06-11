$(document).on('pageinit', function() {
	$(".refresh").click(function() {
		refreshData();
	});
	
	if(localStorage["students"] == undefined) {
		refreshData();
	}	
	
	configurePages();
});

//Refreshes
function refreshData() {
	refreshStudents();
	refreshMatHours();
	refreshProgress();
	refreshLastUpdated();
	configurePages();
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
}

function refreshMatHours() {
	if(localStorage["user"] != undefined) {
		var mathours;
		var user = JSON.parse(localStorage["user"]);
		if(user.barcode == "12345678") {
			mathours = [ 
							{ date: "12/2/2012", class: "BJJ", hours: 1.5  },
							{ date: "12/2/2012", class: "Judo", hours: 1.0  },
							{ date: "12/1/2012", class: "BJJ", hours: 1.5  },  
							{ date: "11/30/2012", class: "BJJ", hours: 1.5  }
					   ];
			localStorage.setItem("mathours", JSON.stringify(mathours));
		}
		else if (user.barcode == "23456789") {
			mathours = [ 
							{ date: "10/2/2012", class: "BJJ", hours: 1.5  },
							{ date: "10/2/2012", class: "Judo", hours: 1.0  },
							{ date: "10/1/2012", class: "BJJ", hours: 1.5  },  
							{ date: "9/30/2012", class: "BJJ", hours: 1.5  }
						];
			localStorage.setItem("mathours", JSON.stringify(mathours));
		}
		else {
			localStorage.removeItem("mathours");
		}

	}
}

function refreshStudents() {
	var students = [ 
						{ name: "Jacob Mims", barcode: "12345678", email: "leingod86@gmail.com", phone: "(480) 466-2686", belt: "Blue", stripes: 2, last_promotion: "12/1/2011" }, 
						{ name: "Billy Boy", barcode: "23456789", email: "billy.bob@gmail.com", phone: "(480) 123-4567", belt: "White", stripes: 1, last_promotion: "11/15/2011" }
				   ];
	localStorage.setItem("students", JSON.stringify(students));
}

function refreshProgress() {
	var progress = 	{ current_belt: "Black", current_hours: 67.5, hours: 150 };
	localStorage.setItem("progress", JSON.stringify(progress));
}

//Setups
function configurePages() {
	setStudents();
	setProfile();
	setMatHours();
	setProgress();
	if(localStorage["lastupdate"] != undefined) {
		$("#last-updated").text(localStorage["lastupdate"]);
	}
	$('.ui-listview').each(function() {
		$(this).listview("refresh");
	});	
}
function setProgress() {
	if(localStorage["progress"] != undefined) {
		var progress = JSON.parse(localStorage["progress"]);
		var percent = (progress.current_hours / progress.hours) * 100;
		$("#progress-knob").html("");
		$("#progress-knob").html('<input class="knob" data-readOnly=true data-data-width="150" data-min="0" data-displayInput=false>');
		$(".knob").attr("data-max", progress.hours);
		$(".knob").val(progress.current_hours);
		$(".knob").attr("data-fgColor", progress.current_belt);
		if(progress.current_belt == "Black") {
				$("#progress-page").attr("background-color", "White");
		}
		$(".knob").knob();
		$("#num-hours").text(progress.current_hours);
		$("#hours-to-promotion").text(progress.hours);
		$("#percentage-to-promotion").text(percent + "%");
	}
}
function setMatHours() {
	if(localStorage["mathours"] != undefined) {
		$("#history-list").html("");
		$("#history-list").append("<li data-role='list-divider'><div class='ui-grid-b'><div class='ui-block-a'>Date</div><div class='ui-block-b'>Class</div><div class='ui-block-c'>Hours</div></div></li>");
		$(JSON.parse(localStorage["mathours"])).each(function() {
			$("#history-list").append("<li><div class='ui-grid-b'><div class='ui-block-a'>" + this.date + "</div><div class='ui-block-b'>" 
				+ this.class + "</div><div class='ui-block-c'>" + this.hours + "</div></div></li>");
		});
	}
}

function setStudents() {
	if(localStorage["students"] != undefined) {
		$("#user-list").html("");
		$(JSON.parse(localStorage["students"])).each(function() {
			$("#user-list").append("<li><a data-corners='false' id='"+ this.barcode + "' data-role='button' onclick='selectUser(" + JSON.stringify(this) + ");'>" + this.name + "</a></li>");
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
	}
}

//Misc
function selectUser(json) {
		localStorage.setItem("user", JSON.stringify(json));
		refreshData();
		$.mobile.changePage("#top-level");
}



